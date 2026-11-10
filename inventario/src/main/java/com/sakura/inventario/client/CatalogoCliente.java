package com.sakura.inventario.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sakura.inventario.exception.RecursoNoEncontradoException;
import com.sakura.inventario.exception.ReglaNegocioException;

@Component
public class CatalogoCliente {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${sakura.catalogo.url}")
	private String baseUrl;

	public void exigirServicio(Long id) {
		try {
			restTemplate.getForObject(baseUrl + "/api/v1/servicios/" + id, String.class);
		} catch (HttpClientErrorException.NotFound ex) {
			throw new RecursoNoEncontradoException("No existe el servicio");
		} catch (RestClientException ex) {
			throw new ReglaNegocioException("No se pudo consultar el catálogo");
		}
	}

}
