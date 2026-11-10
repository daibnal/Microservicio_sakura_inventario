package com.sakura.inventario.exception;

public class NoAutenticadoException extends RuntimeException {
	public NoAutenticadoException(String mensaje) {
		super(mensaje);
	}
}
