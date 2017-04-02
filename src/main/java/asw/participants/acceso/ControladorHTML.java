package asw.participants.acceso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import asw.DBManagement.model.Ciudadano;
import asw.DBManagement.model.Comentario;
import asw.DBManagement.model.Estadistica;
import asw.DBManagement.model.Sugerencia;
import asw.DBManagement.persistence.CiudadanoRepository;
import asw.estadistica.EstadisticaService;

@Controller
public class ControladorHTML {

	 private List<SseEmitter> sseEmitters = Collections.synchronizedList(new ArrayList<>()); 

	
	@Autowired
	private CiudadanoRepository repositorio;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHTML(Model modelo){
		return "login";
	}


	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public String postHTML(@RequestBody String parametros, Model modelo){
		//parametros = email=nombre&password=contraseña
		String[] p = parametros.split("&");

		//Usuario en blanco
		if(p[0].length() <= 8){
			modelo.addAttribute("error", "Usuario en blanco.");
			return "error";
		}

		//Contraseña en blanco
		if(p[1].length() <= 9){
			modelo.addAttribute("error", "Contraseña en blanco.");
			return "error";
		}

		String email = p[0].split("=")[1];
		email = email.replace("%40", "@");
		String password = p[1].split("=")[1];

		//Comprobar los datos

		try{
			Ciudadano ciudadano = repositorio.findByEmail(email);
			if (ciudadano!= null)
			{
				if(!ciudadano.getEmail().equals(email))
				{
					modelo.addAttribute("error", "Email no coincide.");
					return "error";
				}

				if(!ciudadano.getPassword().equals(password))
				{
					modelo.addAttribute("error", "La contraseña no coincide con la del usuario.");
					return "error";
				}

				if(ciudadano != null){
					if(ciudadano.isPrivilegios())
						return this.popularidadSugerencia(parametros, modelo);
					else
						return "user";
				}
			}

			modelo.addAttribute("error", "Usuario no registrado.");
			return "error";

		}catch(Exception e){
			modelo.addAttribute("error", "Ha ocurrido en error al conseguir los datos del usuario.");
			return "error";

		}
	}

	private int edad(String fecha_nac) {     

		Date fechaActual = new Date();
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		String hoy = formato.format(fechaActual);
		String[] dat1 = fecha_nac.split("-");
		String[] dat2 = hoy.split("-");
		int edad = Integer.parseInt(dat2[0]) - Integer.parseInt(dat1[0]);
		int mes = Integer.parseInt(dat2[1]) - Integer.parseInt(dat1[1]);
		if (mes < 0) {
			edad = edad - 1;
		} else if (mes == 0) {
			int dia = Integer.parseInt(dat2[0]) - Integer.parseInt(dat1[0]);
			if (dia > 0) {
				edad = edad - 1;
			}
		}
		return edad;

	}

	@Autowired
	private EstadisticaService estatService;



	public List<Estadistica> popularidadSugerencia(List<Sugerencia> sugerencia) {
		return estatService.listaPopularidadSugerencia(sugerencia);
	}


	@RequestMapping(path="/userPriv", method=RequestMethod.GET)
	public String popularidadSugerencia(@RequestBody String parametros, Model modelo) {
		//metodo que trae una lista usuarios
		List<Sugerencia> sugerencias = new ArrayList<Sugerencia>();
		//Implementar metodo para sacar la lista de usuarios de una misma categoria
		Date fechaActual = new Date();
		long a = 1;
		////sugerencias.add(new Sugerencia(a, "Titulo 1", fechaActual, true, 50));
		////sugerencias.add(new Sugerencia(a, "Titulo 2", fechaActual, false, 25));
		//sugerencias.add(new Sugerencia(a, "Titulo 3", fechaActual, false, 4));
		//sugerencias.add(new Sugerencia(a, "Titulo 4", fechaActual, true, 12));
		System.out.println("Pasa por aqui ");

		List<Estadistica> estadisticas = estatService.listaPopularidadSugerencia(sugerencias);
		modelo.addAttribute("estadisticas",estadisticas);
		return "userPriv";
	}
	
	
	public String nuevaSugerencia(Sugerencia sugerencia,Model modelo)
	{
		if(modelo.containsAttribute("estadisticas")){
			Estadistica esNueva =estatService.nuevaSugerencia(sugerencia);
			@SuppressWarnings("unchecked")
			List<Estadistica> estadisticas = (List<Estadistica>) modelo.asMap().get("estadisticas");
			estadisticas.add(esNueva);
			modelo.addAttribute("estadisticas",estadisticas);
		}
		else
		{
			List<Sugerencia> sugerencias = new ArrayList<Sugerencia>();
			sugerencias.add(sugerencia);
			List<Estadistica> estadisticas = estatService.listaPopularidadSugerencia(sugerencias);	
			modelo.addAttribute("estadisticas",estadisticas);
		}
		this.nuevoHecho();

		return "userPriv";
	}
	
	public String nuevaComentario(Comentario comentario,Model modelo)
	{
		if(modelo.containsAttribute("estadisticas")){
			@SuppressWarnings("unchecked")
			List<Estadistica> estadisticas = (List<Estadistica>) modelo.asMap().get("estadisticas");
			for(Estadistica est : estadisticas){
				
				if(est.getIdSugerencia()==est.getIdSugerencia()){
					estatService.nuevoComentario(comentario, est);
				}
			}
			
			
			modelo.addAttribute("estadisticas",estadisticas);
		}
		
		this.nuevoHecho();
		return "userPriv";
	}
	
	@RequestMapping (path = "/register", method = RequestMethod.GET)
	public SseEmitter register() throws IOException {
	   // log.info("Registering a stream.");

	    SseEmitter emitter = new SseEmitter();

	    synchronized (sseEmitters) {
	        sseEmitters.add(emitter);
	    }
	    emitter.onCompletion(() -> sseEmitters.remove(emitter));

	    return emitter;
	}
	
	
	public void nuevoHecho()
	{
		synchronized (sseEmitters) {
		    sseEmitters.forEach((SseEmitter emitter) -> {
		        try {
		            emitter.send(1);
		        } catch (IOException e) {
		            emitter.complete();
		            sseEmitters.remove(emitter);
		        }
		    });
		}
	}
	
}
