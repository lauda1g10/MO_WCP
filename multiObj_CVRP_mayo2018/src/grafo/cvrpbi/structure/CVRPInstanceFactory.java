package grafo.cvrpbi.structure;

import grafo.optilib.structure.InstanceFactory;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class CVRPInstanceFactory extends InstanceFactory<CVRPInstance> {
    @Override
    public CVRPInstance readInstance(String s) {
        return new CVRPInstance(s);
    }
}
