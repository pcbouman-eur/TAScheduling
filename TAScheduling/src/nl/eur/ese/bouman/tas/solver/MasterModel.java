package nl.eur.ese.bouman.tas.solver;

import java.util.Map;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPConstraint;

import nl.eur.ese.bouman.tas.data.Instance;
import nl.eur.ese.bouman.tas.data.Assistant;
import nl.eur.ese.bouman.tas.data.Session;
;

public class MasterModel
{
	private Instance instance;
	private CLP model;
	
	private Map<Session,CLPConstraint> sessionMap;
	private Map<Assistant,CLPConstraint> assistanMap;
	
	private void initSessionConstraints()
	{

	}
	
}
