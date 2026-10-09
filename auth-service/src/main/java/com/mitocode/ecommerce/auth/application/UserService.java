package com.mitocode.ecommerce.auth.application;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

//Clase de servicio que implementa la interfaz UserDetailsManager para gestionar usuarios en memoria.
//Aqui tendri mi conexion a la base de datos y la logica de negocio para crear, actualizar, eliminar y buscar usuarios.
@Service
@Profile("soloDemo")//Esta clase solo es de muestra, no deseamos activarla en produccion, por eso se le pone el profile soloDemo
public class UserService implements UserDetailsManager {

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
