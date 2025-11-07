package es.fpfempa.acd;

import es.fpfempa.acd.entities.Address;
import es.fpfempa.acd.entities.Customer;
import es.fpfempa.acd.entities.Order;
import es.fpfempa.acd.services.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static OrderService orderService;
    private static Scanner scanner;

    public static void main(String[] args) {
        inicializarRecursos();

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    gestionarDireccionPedido();
                    break;
                case 0:
                    salir = true;
                    System.out.println("¡Hasta luego!");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        }

        cerrarRecursos();
    }

    // método para inicializar todos los recursos que se van a usar
    // el entity Manager, el servicio de order y el scanner
    private static void inicializarRecursos() {
        emf = Persistence.createEntityManagerFactory("acdPU");
        em = emf.createEntityManager();
        orderService = new OrderService(em);
        scanner = new Scanner(System.in);
    }

    // método para cerrarlos
    private static void cerrarRecursos() {
        if (scanner != null) scanner.close();
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Cambiar dirección de pedido");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    private static void gestionarDireccionPedido() {
        System.out.println("\n---- GESTIÓN DE DIRECCIÓN DE PEDIDO ----");
        // Primero se le pide la ID del pedido al usuario
        System.out.print("Introduce el ID del pedido: ");
        Integer orderId = scanner.nextInt();
        scanner.nextLine();

        // Se crea un objeto de tipo order y se comprueba si se ha encontrado el pedido
        Order order = orderService.getOrderDetails(orderId);
        if (order == null) {
            System.out.println("Error: No existe un pedido con ID " + orderId);
            return;
        }

        // Luego si se ha encontrado se muestra la información del pedido
        mostrarInformacionPedido(order);

        // A continuación se pregunta si desea cambiar la dirección
        System.out.print("\n¿Deseas cambiar la dirección de envío? (S/N): ");
        String respuesta = scanner.nextLine().trim().toUpperCase();

        // si dice que no vuelve al menú de nuevo
        if (!respuesta.equals("S")) {
            System.out.println("No se realizaron cambios.");
            return;
        }

        // Ahora aparece un menú que pregunta cómo quieres cambiar la dirección
        System.out.println("\n¿Qué deseas hacer?");
        System.out.println("1. Crear nueva dirección");
        System.out.println("2. Usar dirección existente");
        System.out.print("Elige una opción: ");
        int opcion = scanner.nextInt();
        scanner.nextLine();

        Address nuevaDireccion = null;

        if (opcion == 1) {
            // si elige esta opción se crea una nueva dirección
            nuevaDireccion = crearNuevaDireccion(order.getCustomer());
        } else if (opcion == 2) {
            // si elige esta, le saldrá una lista con las direcciones existentes del cliente para elegir una
            nuevaDireccion = seleccionarDireccionExistente(order.getCustomer());
        } else {
            System.out.println("Opción no válida.");
            return;
        }

        if (nuevaDireccion != null) {
            // Esta es la lógica para actualizar la address del pedido sea cual sea la opción elegida
            try {
                orderService.changeShippingAddress(order, nuevaDireccion);
                System.out.println("\nDirección actualizada correctamente");

                // y verifica que se haya cambiado correctamente
                Order orderActualizado = orderService.getOrderDetails(orderId);
                System.out.println("\nNueva dirección de envío:");
                mostrarDireccion(orderActualizado.getShippingAddress());
            } catch (Exception e) {
                System.out.println("Error al actualizar la dirección: " + e.getMessage());
            }
        }
    }

    // esta función es la que se usa al poner la id del pedido para ver que detalles tiene en ese momento
    private static void mostrarInformacionPedido(Order order) {
        System.out.println("\nINFORMACIÓN DEL PEDIDO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("ID del Pedido: " + order.getId());
        System.out.println("Fecha: " + order.getOrderDate());
        System.out.println("Estado: " + order.getStatus());
        System.out.println("Monto Total: $" + order.getTotalAmount());

        Customer customer = order.getCustomer();
        System.out.println("\n CLIENTE");
        System.out.println("Nombre: " + customer.getFullName());
        System.out.println("Email: " + customer.getEmail());

        Address direccionActual = order.getShippingAddress();
        System.out.println("\nDIRECCIÓN DE ENVÍO ACTUAL");
        if (direccionActual != null) {
            mostrarDireccion(direccionActual);
        } else {
            System.out.println("Sin dirección de envío asignada");
        }
    }

    // y esta es la método que se utiliza para verificar que se ha modificado la dirección correctamente
    private static void mostrarDireccion(Address address) {
        if (address == null) {
            System.out.println("Sin dirección");
            return;
        }
        System.out.println("Calle: " + address.getLine1());
        System.out.println("Ciudad: " + address.getCity());
        System.out.println("País: " + address.getCountry());
    }

    // método para crear una nueva dirección
    private static Address crearNuevaDireccion(Customer customer) {
        System.out.println("\n--- CREAR NUEVA DIRECCIÓN ---");
        System.out.print("Calle (line1): ");
        String line1 = scanner.nextLine();

        System.out.print("Ciudad: ");
        String city = scanner.nextLine();

        System.out.print("País (código de 2 letras, ej: ES, FR, US): ");
        String country = scanner.nextLine().toUpperCase();

        if (country.length() != 2) {
            System.out.println("Error: El código del país debe tener 2 letras");
            return null;
        }

        try {
            Address nuevaAddress = orderService.createNewAddress(customer, line1, city, country);
            System.out.println("Nueva dirección creada correctamente");
            return nuevaAddress;
        } catch (Exception e) {
            System.out.println("Error al crear la dirección: " + e.getMessage());
            return null;
        }
    }


    // el método que se utiliza para que salga la lista de direcciones existentes del cliente y que elija una de ellas
    private static Address seleccionarDireccionExistente(Customer customer) {
        System.out.println("\n--- SELECCIONAR DIRECCIÓN EXISTENTE ---");

        List<Address> direcciones = orderService.getCustomerAddresses(customer);

        if (direcciones == null || direcciones.isEmpty()) {
            System.out.println("Este cliente no tiene direcciones guardadas.");
            System.out.println("Por favor, crea una nueva dirección.");
            return null;
        }

        System.out.println("\nDirecciones disponibles:");
        for (int i = 0; i < direcciones.size(); i++) {
            Address addr = direcciones.get(i);
            System.out.println((i + 1) + ". " + addr.getLine1() + ", " + addr.getCity() + ", " + addr.getCountry());
        }

        System.out.print("\nElige el número de la dirección (1-" + direcciones.size() + "): ");
        int numDireccion = scanner.nextInt();
        scanner.nextLine(); // Limpiar buffer

        if (numDireccion < 1 || numDireccion > direcciones.size()) {
            System.out.println("Número de dirección no válido");
            return null;
        }

        return direcciones.get(numDireccion - 1);
    }
}
