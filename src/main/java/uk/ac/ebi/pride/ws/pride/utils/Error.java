package uk.ac.ebi.pride.ws.pride.utils;


import lombok.Data;

@Data
public class Error {

    private int code;
    private String message;

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
