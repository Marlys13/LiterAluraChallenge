package com.alura.LiterAluraChallenge.principal;

import com.alura.LiterAluraChallenge.model.Autor;
import com.alura.LiterAluraChallenge.model.Libro;
import com.alura.LiterAluraChallenge.model.RespuestaApi;
import com.alura.LiterAluraChallenge.model.record.DatosLibro;
import com.alura.LiterAluraChallenge.repository.IAutorRepository;
import com.alura.LiterAluraChallenge.repository.ILibroRepository;
import com.alura.LiterAluraChallenge.service.ConsumoApiGutendex;
import com.alura.LiterAluraChallenge.service.ConvertirDatos;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoApiGutendex consumoApi = new ConsumoApiGutendex();
    private ConvertirDatos convertir = new ConvertirDatos();
    private static String API_BASE = "https://gutendex.com/books/?search=";
    private List<Libro> datosLibro = new ArrayList<>();
    private ILibroRepository libroRepository;
    private IAutorRepository autorRepository;
    public Principal(ILibroRepository libroRepository, IAutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void consumo(){
        var opcion = -1;
        while (opcion != 0){
            var menu = """
                    ¡Bienvendido a LiterAlura!
                    
                    1 - Agregar Libro por Nombre
                    2 - Libros buscados
                    3 - Buscar libro por Nombre
                    4 - Buscar todos los Autores de libros buscados
                    5 - Buscar Autores por año
                    6 - Buscar Libros por Idioma
                    7 - Top 10 Libros mas Descargados
                    8 - Buscar Autor por Nombre
                   
                    
               
                    0 - Salir
                    
                    Por favor, seleccione una opción.
                    
                    """;

            try {
                System.out.println(menu);
                opcion = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Por favor, ingrese una opción válida. ");
                sc.nextLine();
                continue;
            }



            switch (opcion){
                case 1:
                    buscarLibroEnLaWeb();
                    break;
                case 2:
                    librosBuscados();
                    break;
                case 3:
                    buscarLibroPorNombre();
                    break;
                case 4:
                    BuscarAutores();
                    break;
                case 5:
                    buscarAutoresPorAnio();
                    break;
                case 6:
                    buscarLibrosPorIdioma();
                    break;
                case 7:
                    top10LibrosMasDescargados();
                    break;
                case 8:
                    buscarAutorPorNombre();
                    break;
                case 0:
                    opcion = 0;
                    System.out.println("Hasta la próxima! ");
                    break;
                default:
                    System.out.println("Opción Incorrecta. ");
                    System.out.println("Por favor elija otra opción");
                    consumo();
                    break;
            }
        }
    }

    private Libro getDatosLibro(){
        System.out.println("Ingrese el nombre del libro: ");
        var nombreLibro = sc.nextLine().toLowerCase();
        var json = consumoApi.obtenerDatos(API_BASE + nombreLibro.replace(" ", "%20"));
        RespuestaApi datos = convertir.convertirDatosJsonAJava(json, RespuestaApi.class);

        if (datos != null && datos.getResultadoLibros() != null && !datos.getResultadoLibros().isEmpty()) {
            DatosLibro primerLibro = datos.getResultadoLibros().get(0);
            return new Libro(primerLibro);
        } else {
            System.out.println("No se encontraron resultados.");
            return null;
        }
    }


    private void buscarLibroEnLaWeb() {
        Libro libro = getDatosLibro();

        if (libro == null){
            System.out.println("Libro no encontrado. el valor es null");
            return;
        }

        try{
            boolean libroExists = libroRepository.existsByTitulo(libro.getTitulo());
            if (libroExists){
                System.out.println("El libro ya existe en la base de datos!");
            }else {
                libroRepository.save(libro);
                System.out.println(libro.toString());
            }
        }catch (InvalidDataAccessApiUsageException e){
            System.out.println("No se puede persisitir el libro buscado!");
        }
    }

    @Transactional(readOnly = true)
    private void librosBuscados(){
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en la base de datos.");
        } else {
            System.out.println("Libros encontrados en la base de datos:");
            for (Libro libro : libros) {
                System.out.println(libro.toString());
            }
        }
    }

    private void buscarLibroPorNombre() {
        System.out.println("Ingrese Titulo libro que quiere buscar: ");
        var titulo = sc.nextLine();
        Libro libroBuscado = libroRepository.findByTituloContainsIgnoreCase(titulo);
        if (libroBuscado != null) {
            System.out.println("El libro buscado fue: " + libroBuscado);
        } else {
            System.out.println("El libro con el titulo '" + titulo + "' no se encontró.");
        }
    }

    private  void BuscarAutores(){
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("No se encontraron libros en la base de datos. \n");
        } else {
            System.out.println("Libros encontrados en la base de datos: \n");
            Set<String> autoresUnicos = new HashSet<>();
            for (Autor autor : autores) {
                if (autoresUnicos.add(autor.getNombre())){
                    System.out.println(autor.getNombre()+'\n');
                }
            }
        }
    }

    private void  buscarLibrosPorIdioma(){
        System.out.println("Ingrese Idioma en el que quiere buscar: \n");
        System.out.println("|***********************************|");
        System.out.println("|  Opción - es : Libros en español. |");
        System.out.println("|  Opción - en : Libros en ingles.  |");
        System.out.println("|***********************************|\n");

        var idioma = sc.nextLine();
        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en la base de datos.");
        } else {
            System.out.println("Libros segun idioma encontrados en la base de datos:");
            for (Libro libro : librosPorIdioma) {
                System.out.println(libro.toString());
            }
        }

    }

    private void buscarAutoresPorAnio() {

        System.out.println("Indica el año para consultar que autores estan vivos: \n");
        var anioBuscado = sc.nextInt();
        sc.nextLine();

        List<Autor> autoresVivos = autorRepository.findByCumpleaniosLessThanOrFechaFallecimientoGreaterThanEqual(anioBuscado, anioBuscado);

        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores que estuvieran vivos en el año " + anioBuscado + ".");
        } else {
            System.out.println("Los autores que estaban vivos en el año " + anioBuscado + " son:");
            Set<String> autoresUnicos = new HashSet<>();

            for (Autor autor : autoresVivos) {
                if (autor.getCumpleanios() != null && autor.getFechaFallecimiento() != null) {
                    if (autor.getCumpleanios() <= anioBuscado && autor.getFechaFallecimiento() >= anioBuscado) {
                        if (autoresUnicos.add(autor.getNombre())) {
                            System.out.println("Autor: " + autor.getNombre());
                        }
                    }
                }
            }
        }
    }

    private void top10LibrosMasDescargados(){
        List<Libro> top10Libros = libroRepository.findTop10ByTituloByCantidadDescargas();
        if (!top10Libros.isEmpty()){
            int index = 1;
            for (Libro libro: top10Libros){
                System.out.printf("Libro %d: %s Autor: %s Descargas: %d\n",
                        index, libro.getTitulo(), libro.getAutores().getNombre(), libro.getCantidadDescargas());
                index++;
            }
        }
    }


    private void buscarAutorPorNombre() {
        System.out.println("Ingrese nombre del escritor que quiere buscar: ");
        var escritor = sc.nextLine();
        Optional<Autor> escritorBuscado = autorRepository.findFirstByNombreContainsIgnoreCase(escritor);
        if (escritorBuscado != null) {
            System.out.println("\nEl escritor buscado fue: " + escritorBuscado.get().getNombre());

        } else {
            System.out.println("\nEl escritor con el titulo '" + escritor + "' no se encontró.");
        }
    }
}
