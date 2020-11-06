package com.PCLP2.Project.data.dao;

import com.PCLP2.Project.data.entity.Book;
import com.PCLP2.Project.data.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    /**
     * gaseste toate cartile din baza de date
     * @return toate cartile din baza de date
     */
    public static List<Book> findAll() {
        List<Book> rez = new ArrayList<>();

        Connection con = DBConnection.openConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = "select books.id, bname as 'bookName', concat(firstName, ' ',lastName) as 'authorName', gType as 'genre', formats.format, years.releaseYear, ISBN, ISSN, locationCode from books, authors, genres, formats, years " +
                "where books.authorID = authors.id and books.genreID = genres.gId and books.formatID = formats.id and books.yearID = years.id;";

        try {
            ps = con.prepareStatement(sqlQuery);
            rs = ps.executeQuery();

            while (rs.next()) {
                Book bk = new Book();

                bk.setId(rs.getInt("id"));
                bk.setBookName(rs.getString("bookName"));
                bk.setAuthorName(rs.getString("authorName"));
                bk.setGenre(rs.getString("genre"));
                bk.setFormat(rs.getString("format"));
                bk.setReleaseYear(rs.getInt("releaseYear"));
                bk.setISBN(rs.getString("ISBN"));
                bk.setISSN(rs.getString("ISSN"));
                bk.setLocationCode(rs.getString("locationCode"));

                rez.add(bk);
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }

        DBConnection.closeConnection(con, ps, rs);

        return rez;
    }

    /**
     * adauga o carte in baza de date: verifica daca exista deja autorul, genul, formatul, anul lansarii, daca nu
     * daca nu adauga parametrii lipsa
     *
     * @param book o noua carte
     * @return true - daca operatie s-a realizat cu success, false - daca nu
     */
    public static boolean addBook(Book book) {

        int authorID, genreID, formatID, yearID;
        boolean rez = false;

        Connection con = DBConnection.openConnection();
        PreparedStatement ps = null;

        try {

            // gasim indexul autorului , daca nu este il adaugam
            if((authorID = getID(con, "authors", book.getAuthorName())) == -1) {
                if(!insertAuthor(con, book.getAuthorName().split(" ")))
                    throw new Exception("Author insert error");
                authorID = getID(con, "authors", book.getAuthorName());
            }

            // gasim indexul genului, daca nu este il adaugam
            if( (genreID = getID(con, "genres", book.getGenre())) == -1)  {
                if(!insertGenre(con, book.getGenre()))
                    throw new Exception("Genre insert error");
                genreID = getID(con, "genres", book.getGenre());
            }

            //gasim indexul formatului, daca nu este il adaugam
            if( (formatID = getID(con, "formats", book.getFormat())) == -1)  {
                if(!insertFormat(con, book.getFormat()))
                    throw new Exception("Format insert error");
                formatID = getID(con, "formats", book.getFormat());
            }

            //gasim indexul anului, daca nu este il adaugam
            if( (yearID = getID(con, "years", ("" + book.getReleaseYear()))) == -1)  {
                if(!insertReleaseYear(con, book.getReleaseYear()))
                    throw new Exception("Year insert error");
                yearID = getID(con, "years", ("" + book.getReleaseYear()));
            }

            //verificam daca codul ISBN este unic
            int returnNo;
            if( (returnNo = checkUniqueCode(con, "ISBN", book.getISBN())) != 0)
                if(returnNo == -1)
                    throw new Exception("Input error ISBN");
                else
                    throw new Exception("ISBN code is not unique");

            // vericam daca codul ISSN este unic
            if( (returnNo = checkUniqueCode(con, "ISSN", book.getISSN())) != 0)
                if(returnNo == -1)
                    throw new Exception("Input error ISSN");
                else
                    throw new Exception("ISSN code is not unique");


            //verificam daca codul de locatie este unic, daca nu generam altul si il adaugam
            do {
                if ((returnNo = checkUniqueCode(con, "locationCode", book.getLocationCode())) != 0)
                    if (returnNo == -1) {
                        throw new Exception("Input error locationCode");
                    } else
                        book.regenerateLocCode();
            } while (returnNo != 0);


            ps = con.prepareStatement("insert into library_db.books(bname, authorID, genreID, formatID, yearID, ISBN, ISSN, locationCode, id) values (?, ?, ?, ?, ?, ?, ?, ?, null);");
            ps.setString(1, book.getBookName());
            ps.setInt(2, authorID);
            ps.setInt(3,genreID);
            ps.setInt(4, formatID);
            ps.setInt(5, yearID);
            ps.setString(6,book.getISBN());
            ps.setString(7, book.getISSN());
            ps.setString(8, book.getLocationCode());

            if(ps.executeUpdate() != 1)
                throw new Exception("Book insert error");

            rez = true;

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        DBConnection.closeConnection(con, ps, null);

        return rez;

    }

    /**
     *
     * sterge o carte din baza de date dupa ISBN/ISSN/cod loatie
     *
     * @param book carte care se afla in baza de date
     * @return true - daca stergerea a reusit, false daca nu
     */
    public static boolean removeBook(Book book) {

        Connection con = DBConnection.openConnection();
        PreparedStatement ps = null;
        boolean rez = false;

        try {
            ps = con.prepareStatement("delete from books where ISBN = ? or ISSN = ? or locationCode = ? and id > 0;");
            ps.setString(1, book.getISBN());
            ps.setString(2, book.getISSN());
            ps.setString(3, book.getLocationCode());
            if(ps.executeUpdate() != 1)
                throw new Exception("Error trying to remove the book");
            rez = true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        DBConnection.closeConnection(con, ps, null);

        return rez;
    }

    /**
     *cautere in baza de date dupa un cuvant sau mai multe care poate fi numele cartii, autorul, genul, formatul, anul lansarii,
     * codul ISBN, ISSN si codul de locatie
     *
     * @param bookRelated cuvant sau mai multe cuvinte cheie pentru cautarea in baza de date
     * @return Lista de carti
     */
    public static List<Book> searchBook(String bookRelated) {

        Connection con = DBConnection.openConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String[] wordsToSearch = bookRelated.split(" ");
        List<Book> rez = new ArrayList<>();

        String query = "select books.id, bname, firstName, lastName, gType, `format`, releaseYear, ISBN, ISSN, locationCode\n" +
                "from books, authors, genres, formats, years\n" +
                "where (\n" +
                "\tbname like ? or\n" +
                "\tfirstName = ? or\n" +
                "    lastName = ? or\n" +
                "    gType = ? or\n" +
                "    `format`= ? or\n" +
                "    releaseYear = ? or\n" +
                "    ISBN = ? or\n" +
                "    ISSN = ? or\n" +
                "    locationCode = ? )\n" +
                "and ( books.authorID = authors.id and books.formatID = formats.id and books.genreID = genres.gId and books.yearID = years.id);";

        for (String toSearch : wordsToSearch) {
            try {
                ps = con.prepareStatement(query);
                ps.setString(1, "%" + toSearch + "%");
                ps.setString(2, toSearch);
                ps.setString(3, toSearch);
                ps.setString(4, toSearch);
                ps.setString(5, toSearch);
                ps.setString(6, toSearch);
                ps.setString(7, toSearch);
                ps.setString(8, toSearch);
                ps.setString(9, toSearch);
                rs = ps.executeQuery();

                while (rs.next()) {
                    Book book = new Book();

                    book.setId(rs.getInt("id"));
                    book.setBookName(rs.getString("bname"));
                    book.setAuthorName(rs.getString("firstName") + " " + rs.getString("lastName") );
                    book.setGenre(rs.getString("gType"));
                    book.setFormat(rs.getString("format"));
                    book.setReleaseYear(rs.getInt("releaseYear"));
                    book.setISBN(rs.getString("ISBN"));
                    book.setISSN(rs.getString("ISSN"));
                    book.setLocationCode(rs.getString("locationCode"));

                    rez.add(book);
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
                break;
            }
        }

        DBConnection.closeConnection(con, ps, rs);

        return rez;
    }

    /**
     * actualizeaza un parametru al unei carti din baza de date
     *
     * @param bookId id-ul cartii in baza de date
     * @param column coloana unde trebie facuta schimbarea
     * @param newValue noua valoare
     * @return true - daca operatie s-a realizat cu succes, false - daca nu
     */
    public static boolean updateBook(int bookId, String column, String newValue) {
        boolean rez = false;

        Connection con = DBConnection.openConnection();
        PreparedStatement ps = null;

        try {

            switch (column) {
                case "bname":
                    ps = con.prepareStatement("update library_db.books set bname = ? where id = ?;");
                    ps.setString(1, newValue);
                    ps.setInt(2, bookId);
                    if(ps.executeUpdate() == 1)
                        rez = true;
                    break;
                case "author":
                    int authorID = getID(con, "authors", newValue);
                    if(authorID == -1)
                        if(insertAuthor(con, newValue.split(" "))) {
                            authorID = getID(con, "authors", newValue);
                            ps = con.prepareStatement("update library_db.books set authorID = ? where id = ?;");
                            ps.setInt(1, authorID);
                            ps.setInt(2, bookId);
                            if(ps.executeUpdate() == 1)
                                rez = true;
                        }
                        else
                            System.err.println("Eroare in adaugarea noului autor");
                    else {
                        ps = con.prepareStatement("update library_db.books set authorID = ? where id = ?;");
                        ps.setInt(1, authorID);
                        ps.setInt(2, bookId);
                        if(ps.executeUpdate() == 1)
                            rez = true;
                    }
                    break;
                case "genre":
                    int genreID = getID(con, "genres", newValue);
                    if(genreID == -1)
                        if(insertGenre(con, newValue)) {
                            genreID = getID(con, "genres", newValue);
                            ps = con.prepareStatement("update library_db.books set genreID = ? where id = ?;");
                            ps.setInt(1, genreID);
                            ps.setInt(2, bookId);
                            if(ps.executeUpdate() == 1)
                                rez = true;
                        }
                        else
                            System.err.println("Eroare in adaugarea noului gen");
                    else {
                        ps = con.prepareStatement("update library_db.books set genreID = ? where id = ?;");
                        ps.setInt(1, genreID);
                        ps.setInt(2, bookId);
                        if(ps.executeUpdate() == 1)
                            rez = true;
                    }
                    break;
                case "format":
                    int formatID = getID(con, "formats", newValue);
                    if(formatID == -1)
                        if(insertFormat(con, newValue)) {
                            formatID = getID(con, "formats", newValue);
                            ps = con.prepareStatement("update library_db.books set fotmatID = ? where id = ?;");
                            ps.setInt(1, formatID);
                            ps.setInt(2, bookId);
                            if(ps.executeUpdate() == 1)
                                rez = true;
                        }
                        else
                            System.err.println("Eroare in adaugarea noului format");
                    else {
                        ps = con.prepareStatement("update library_db.books set fotmatID = ? where id = ?;");
                        ps.setInt(1, formatID);
                        ps.setInt(2, bookId);
                        if(ps.executeUpdate() == 1)
                            rez = true;
                    }
                    break;
                case "releaseYear":
                    int releaseYear = getID(con, "years", newValue);
                    if(releaseYear == -1)
                        if(insertReleaseYear(con, Integer.parseInt(newValue))) {
                            releaseYear = getID(con, "years", newValue);
                            ps = con.prepareStatement("update library_db.books set yearID = ? where id = ?;");
                            ps.setInt(1, releaseYear);
                            ps.setInt(2, bookId);
                            if(ps.executeUpdate() == 1)
                                rez = true;
                        }
                        else
                            System.err.println("Eroare in adaugarea noului an");
                    else {
                        ps = con.prepareStatement("update library_db.books set yearID = ? where id = ?;");
                        ps.setInt(1, releaseYear);
                        ps.setInt(2, bookId);
                        if(ps.executeUpdate() == 1)
                            rez = true;
                    }
                    break;
                default:
                    System.err.println("Nu poti modifica codurile ISBN/ISSN/locationCode");
            }

        }
        catch (NumberFormatException ignored) { }
        catch (Exception e) {
            System.err.println("Error trying to update the book: " + e.getMessage());
       }
        DBConnection.closeConnection(con, ps, null);

        return rez;
    }

    private static boolean insertAuthor(Connection con, String[] authorname) {

        if(authorname.length != 2) return false;

        PreparedStatement ps;
        boolean rez = false;

        try {
            ps = con.prepareStatement("insert into library_db.authors(firstName, lastName, id) values(?, ?, null);");
            ps.setString(1, authorname[0].toLowerCase());
            ps.setString(2, authorname[1].toLowerCase());
            if (ps.executeUpdate() == 1)
                rez = true;
        } catch (Exception e) {
            System.err.println("Query insertAuthor error: " + e.getMessage());
        }

        return rez;
    }

    private static boolean insertGenre(Connection con, String genre) {

        PreparedStatement ps;
        boolean rez = false;

        try {
            ps = con.prepareStatement("insert into library_db.genres(gId, gType) values (null, ?);");
            ps.setString(1, genre.toLowerCase());
            if (ps.executeUpdate() == 1)
                rez = true;
        } catch (Exception e) {
            System.err.println("Query insertGenre error: " + e.getMessage());
        }

        return rez;
    }

    private static boolean insertFormat(Connection con, String format) {
        PreparedStatement ps;
        boolean rez = false;

        try {
            ps = con.prepareStatement("insert into library_db.formats(id, `format`) values (null, ?);");
            ps.setString(1, format.toLowerCase());
            if (ps.executeUpdate() == 1)
                rez = true;
        } catch (Exception e ) {
            System.err.println("Query insertFormat error: " + e.getMessage());
        }

        return rez;
    }

    private static boolean insertReleaseYear(Connection con, int releaseYear) {
        PreparedStatement ps;
        boolean rez = false;

        try {
            ps = con.prepareStatement("insert into library_db.years(id, releaseYear) values (null, ?);");
            ps.setInt(1, releaseYear);
            if (ps.executeUpdate() == 1)
                rez = true;
        } catch (Exception e) {
            System.err.println("Query insertYear error: " + e.getMessage());
        }

        return rez;
    }



        private static int getID(Connection con, String table, String getIdOf) {

        PreparedStatement ps;
        ResultSet rs;

        int searchId = -1;

        try {
            switch (table) {
                case "genres":
                    ps = con.prepareStatement("select gId from library_db.genres where gType = ?");
                    ps.setString(1, getIdOf.toLowerCase());
                    rs = ps.executeQuery();
                    if (rs.next())
                        searchId = rs.getInt("gId");
                    break;

                case "authors":
                    ps = con.prepareStatement("select id from library_db.authors where concat(firstName, ' ', lastName) = ?");
                    ps.setString(1, getIdOf.toLowerCase());
                    rs = ps.executeQuery();
                    if (rs.next())
                        searchId = rs.getInt("id");
                    break;

                case "formats":
                    ps = con.prepareStatement("select id from library_db.formats where format = ?; ");
                    ps.setString(1, getIdOf.toLowerCase());
                    rs = ps.executeQuery();
                    if (rs.next())
                        searchId = rs.getInt("id");
                    break;

                case "years":
                    ps = con.prepareStatement("select id from library_db.years where releaseYear = ?;");
                    ps.setInt(1, Integer.parseInt(getIdOf));
                    rs = ps.executeQuery();
                    if(rs.next())
                        searchId = rs.getInt("id");
            }
        }
        catch (NumberFormatException ignored) { }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return searchId;

    }

    private static int checkUniqueCode(Connection con, String ISBNorISSNorLocationCode, String code) {

        PreparedStatement ps;
        ResultSet rs;

        int res = -1;

        switch (ISBNorISSNorLocationCode) {
            case "ISBN":
                if (code.length() != 19)
                    return res;
                else {
                    try {
                        ps = con.prepareStatement("select count(id) from library_db.books where ISBN = ?;");
                        ps.setString(1, code);
                        rs = ps.executeQuery();
                        if (rs.next())
                            res = rs.getInt("count(id)");
                    } catch (Exception ignored) {
                    }
                }
                break;
            case "ISSN":
                if (code.length() != 9)
                    return res;
                else {
                    try {
                        ps = con.prepareStatement("select count(id) from library_db.books where ISSN = ?;");
                        ps.setString(1, code);
                        rs = ps.executeQuery();
                        if (rs.next())
                            res = rs.getInt("count(id)");
                    } catch (Exception ignored) {
                    }
                }
                break;
            case "locationCode":
                if (code.length() != 5)
                    return res;
                else {
                    try {
                        ps = con.prepareStatement("select count(id) from library_db.books where locationCode = ?;");
                        ps.setString(1, code);
                        rs = ps.executeQuery();
                        if (rs.next())
                            res = rs.getInt("count(id)");
                    } catch (Exception ignored) {
                    }
                }
                break;
        }
        return res;
    }
}
