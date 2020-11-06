package com.PCLP2.Project;


import com.PCLP2.Project.data.dao.BookDAO;
import com.PCLP2.Project.data.entity.Book;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static Book selectedBook;

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        do {
            int optiune = mainMenu(scan);
            if(optiune == 6)
                break;
            else if(optiune == 1)
                showAllBooks(scan);
            else if (optiune == 2)
                addBook();
            else if(optiune == 3)
                findBook(scan);
            else if(optiune == 4)
                updateBook(scan);
            else if(optiune == 5)
                removeBook(scan);


        } while (true);

        scan.close();
    }

    private static int mainMenu(Scanner scan) {
        if(selectedBook == null) {
            System.out.println("Buna venit!");
        } else {
            System.out.println("Carte selectata: " + selectedBook);
        }
        System.out.println("1. Afisati toate cartile\n2. Adaugati o carte noua\n3. Cautati carti\n4. Modificati cartea selectata\n5. Stergeti cartea selectata\n6. Iesire");
        System.out.println("NOTE:Pentru a iesi din orice meniu apasati 0.");
        String n;
        int op;

        do {

            try {
                System.out.print("::");
                n = scan.next();
                op = Integer.parseInt(n);
                if(op < 1 || op > 6)
                    throw new Exception();
                break;
            } catch (Exception ignored) {
                scan.nextLine();
                System.out.println("Introdu un numar de la 1 la 6");
            }


        } while (true);
        return op;
    }

    private static void showAllBooks(Scanner scanner) {
        List<Book> books = BookDAO.findAll();

        for (int i = 0; i<books.size();i++)
            System.out.println(i+1 + ". " + books.get(i));

        int op;

        do{
            try {
                System.out.print("Selecteaza o carte: ");
                op = scanner.nextInt();
                if(op == 0)
                    return;
                if(op < 1 || op > books.size())
                    throw new Exception("Selecteaza o carte de la 1 la " + books.size());
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);

        selectedBook = books.get(op-1);
    }

    private static void addBook() {

        Scanner scanner = new Scanner(System.in);

        String numeCarte, numeAutor, gen, format, ISBN, ISSN;
        int year;

        System.out.println("Adaugati cartea: \n");
        do {
            try {
                System.out.print("Numele cartii: ");
                numeCarte = scanner.nextLine();
                if(numeCarte.equals("0"))
                    return;
                if(numeCarte.matches("[\\W]+") || numeCarte.isBlank())
                    throw new Exception();
                break;

            }catch (Exception ignored) {
                System.out.println("Numele nu este valid");
            }
        } while (true);

        do {
            try {
                System.out.print("Autorul cartii: ");
                numeAutor = scanner.nextLine();
                if(numeAutor.equals("0"))
                    return;
                if(numeAutor.matches("[\\W]+") || numeAutor.isBlank())
                    throw new Exception();
                break;

            }catch (Exception ignored) {
                System.out.println("Numele nu este valid");
            }
        } while (true);

        do {
            try {
                System.out.print("Genul cartii: ");
                gen = scanner.next();
                if(gen.equals("0"))
                    return;
                if(gen.matches("[\\W]+") || gen.isBlank())
                    throw new Exception();
                break;

            }catch (Exception ignored) {
                System.out.println("Genul nu este valid");
            }
        } while (true);

        do {
            try {
                System.out.print("Anul lansarii: ");
                year = scanner.nextInt();
                if(year == 0)
                    return;
                if(year/10000 != 0)
                    throw new Exception();
                break;
            }catch (Exception ignored) {
                System.out.println("Anul nu este valid");
            }
        } while (true);

        do {
            try {
                System.out.print("Formatul cartii: ");
                format = scanner.next();
                if(format.equals("0"))
                    return;
                if(format.matches("[\\W]+") || format.isBlank())
                    throw new Exception();
                break;

            }catch (Exception ignored) {
                System.out.println("Formatul nu este valid");
            }
        } while (true);


        do {
            try {
                System.out.print("ISBN carte: ");
                ISBN = scanner.next();
                if(ISBN.equals("0"))
                    return;
                if(ISBN.matches("[\\W]+") || ISBN.isBlank())
                    throw new Exception();
                if (ISBN.length() != 19) {
                    System.out.println("Lungimea trebuie sa fie de 19\n1234-5678-9123-4567");
                    throw new Exception();
                }
                break;
            }catch (Exception ignored) {
                System.out.println("ISBN nu este valid");
            }
        } while (true);


        do {
            try {
                System.out.print("ISSN carte: ");
                ISSN = scanner.next();
                if(ISSN.equals("0"))
                    return;
                if(ISSN.matches("[\\W]+") || ISSN.isBlank())
                    throw new Exception();
                if (ISSN.length() != 9) {
                    System.out.println("Lungimea trebuie sa fie de 9\n1234-5678");
                    throw new Exception();
                }
                break;
            }catch (Exception ignored) {
                System.out.println("ISSN nu este valid");
            }
        } while (true);

        Book newBok = new Book(numeCarte, numeAutor, gen, format, year,ISBN, ISSN);
        newBok.regenerateLocCode();

        if(BookDAO.addBook(newBok)) {
            System.out.println("Adaugare realizata!");
            selectedBook = newBok;
        } else {
            System.out.println("Adaugarea a esuat!");
        }

    }

    private static void removeBook(Scanner scanner) {
        if(selectedBook == null) {
            System.out.println("Nu ai selectat nicio carte");
            findBook(scanner);
        } else {
            if(BookDAO.removeBook(selectedBook)) {
                System.out.println("Stergerea s-a realizat cu succes");
                selectedBook = null;
            }
            else
                System.out.println("Eroare in timpul stergerii");
        }
    }


    private static void findBook(Scanner scanner) {
        List<Book> foundBooks;
        String bookRelated;
        int op;

        do {
            try {
                System.out.print("Cautare: ");
                scanner.nextLine();
                bookRelated = scanner.nextLine();
                if(bookRelated.equals("0"))
                    return;
                if(bookRelated.matches("[\\W]+"))
                    throw new Exception("Simbolurile nu sunt permise");
                if(bookRelated.isBlank())
                    throw new Exception("Trebuie sa introduci minim 1 cuvant");
                foundBooks= BookDAO.searchBook(bookRelated);
                if(foundBooks.isEmpty())
                    throw new Exception("Nu s-a gasit nicio carte");
                for(int i = 0; i < foundBooks.size(); i++)
                    System.out.println(i+1 + ". " + foundBooks.get(i) );
                break;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);

        do {
            try {
                System.out.print("Selecteaza cartea: ");
                bookRelated = scanner.next();
                if(bookRelated.equals("0"))
                    return;
                op = Integer.parseInt(bookRelated);
                if(op < 1 || op > foundBooks.size() )
                    throw new Exception("Alege o carte de la 1 la " + foundBooks.size());
                selectedBook = foundBooks.get(op-1);
                break;
            }
            catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
            catch (Exception ignored) {
                System.out.println("Alegere invalida");
            }

        } while (true);

    }

    private static void updateBook(Scanner scanner) {
        if(selectedBook == null) {
            System.out.println("Trebuie sa aveti o carte selectata");
            return;
        }

        String newValue, n;
        int coloana;

        System.out.println("Selectati coloana pe care o modificati:\n1.Numele cartii\n2.Autorul\n3.Genul\n4.Fomatul\n5.Anul lansarii\n6.Iesire");
        do {
            try {

                System.out.print("::");
                n = scanner.next();
                coloana = Integer.parseInt(n);
                if(coloana < 1 || coloana > 6)
                    throw new Exception("Alege un numar de la 1 la 6");
                if(coloana == 6)
                    return;
                break;

            }
            catch (NumberFormatException ignored) {
                System.out.println("Trebuie sa alege un numar de la 1 la 6");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);
        scanner.nextLine();
        do {
            try {

                System.out.print("Valoare noua: ");
                newValue = scanner.nextLine();
                if(newValue.isBlank())
                    throw new Exception("Trebuie sa introduci o valoare");
                if(coloana == 5) {
                    Integer.parseInt(newValue);
                }
                if(newValue.matches("[\\W]+"))
                    throw new Exception("Simbourile nu sunt permise");
                break;

            }
            catch (NumberFormatException ignored) {
                System.out.println("Pentru an trebuie sa folosesti numai numere");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (true);

        if(coloana == 1) {
            if(BookDAO.updateBook(selectedBook.getId(), "bname", newValue)) {
                System.out.println("Actualizarea cartii a reusit");
                selectedBook.setBookName(newValue);
            }
            else
                System.out.println("Actulizarea a esuat");
        }
        else if (coloana == 2) {
            if (BookDAO.updateBook(selectedBook.getId(), "author", newValue)) {
                System.out.println("Actualizarea cartii a reusit");
                selectedBook.setAuthorName(newValue);
            }
            else
                System.out.println("Actulizarea a esuat");
        }
        else if (coloana == 3) {
            if (BookDAO.updateBook(selectedBook.getId(), "genre", newValue)) {
                System.out.println("Actualizarea cartii a reusit");
                selectedBook.setGenre(newValue);
            }
            else
                System.out.println("Actulizarea a esuat");
        }
        else if (coloana == 4) {
            if (BookDAO.updateBook(selectedBook.getId(), "format", newValue)) {
                System.out.println("Actualizarea cartii a reusit");
                selectedBook.setFormat(newValue);
            }
            else
                System.out.println("Actulizarea a esuat");
        }
        else {
            if (BookDAO.updateBook(selectedBook.getId(), "releaseYear", newValue)) {
                System.out.println("Actualizarea cartii a reusit");
                selectedBook.setReleaseYear(Integer.parseInt(newValue));
            } else
                System.out.println("Actulizarea a esuat");
        }
    }
}
