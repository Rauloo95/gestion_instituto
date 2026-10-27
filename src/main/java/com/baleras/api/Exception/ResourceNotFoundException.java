package com.baleras.api.Exception;

public class ResourceNotFoundException extends RuntimeException{
    //La utilizaremos cuando alguien intente acceder a algo que no existe.

    public ResourceNotFoundException(String message){
        super(message);
    }
}
