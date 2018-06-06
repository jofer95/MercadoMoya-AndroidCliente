package com.example.jorgebarraza.mercadomoya_cliente.DB;

public class Servicios {

    //SERVICIOS DE LA APP
    static String baseURL = "https://mercadomoya.azurewebsites.net/api/";

    //Articulos:
    public static String obtenerTodosLosArticulos(){
        return baseURL + "Articulos/ObtenerTodosLosArticulos";
    }

    public static String obtenerArticuloPorID(){
        return baseURL + "Articulos/ObtenerArticuloPorID";
    }

    public static String crearActualizarArticulo(){
        return baseURL + "Articulos/CrearActualizarArticulo";
    }

    //Categorias:
    public static String obtenerTodasLasCategorias(){
        return baseURL + "Categorias/ObtenerTodasLasCategorias";
    }

    public static String obtenerCategoriaPorID(){
        return baseURL + "Categorias/ObtenerCategoriaPorID";
    }

    public static String crearActualizarCategoria(){
        return baseURL + "Categorias/CrearActualizarCategoria";
    }

    //Usuarios:
    public static String obtenerTodosLosUsuarios(){
        return baseURL + "Usuarios/ObtenerTodosLosUsuarios";
    }

    public static String obtenerUsuarioPorID(){
        return baseURL + "Usuarios/ObtenerUsuarioPorID";
    }

    public static String obtenerUsuarioPorCorreo(){
        return baseURL + "Usuarios/ObtenerUsuarioPorCorreo";
    }

    public static String crearActualizarUsuario(){
        return baseURL + "Usuarios/CrearActualizarUsuario";
    }

    //Pedidos:
    public static String obtenerTodosLosPedidos(){
        return baseURL + "Pedidos/ObtenerTodosLosPedidos";
    }

    public static String obtenerPedidoPorID(){
        return baseURL + "Pedidos/ObtenerPedidoPorID";
    }

    public static String obtenerPedidoPorUsuario(){
        return baseURL + "Pedidos/ObtenerPedidoPorUsuario";
    }

    public static String obtenerUsuarioPorIDTemp(){
        return baseURL + "Pedidos/ObtenerPedidoPorIDTemp";
    }

    public static String crearActualizarPedido(){
        return baseURL + "Pedidos/CrearActualizarPedido";
    }
}