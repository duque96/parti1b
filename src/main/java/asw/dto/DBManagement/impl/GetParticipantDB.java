package asw.dto.DBManagement.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import asw.dto.DBManagement.GetParticipant;
import asw.dto.DBManagement.persistence.CiudadanoRepository;
import asw.dto.model.CitizenDB;
import asw.participants.acceso.ParticipantsLogin;

@Component
public class GetParticipantDB implements GetParticipant{

	@Autowired
	private CiudadanoRepository repositorio; 
	
	@Override
	public CitizenDB getCiudadano(String mail) {
		CitizenDB citizen = repositorio.findByEmail(mail);
		return citizen;
	}

	@Override
	public CitizenDB getCiudadano(ParticipantsLogin participante) {
		CitizenDB citizen = repositorio.findByEmail(participante.getEmail());
		return citizen;
	}


}
