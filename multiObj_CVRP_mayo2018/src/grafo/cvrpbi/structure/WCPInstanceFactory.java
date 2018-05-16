package grafo.cvrpbi.structure;

import grafo.optilib.structure.InstanceFactory;

public class WCPInstanceFactory extends InstanceFactory<WCPInstance> {
	@Override
	public WCPInstance readInstance(String s) {
		 return new WCPInstance(s);
	}
}
