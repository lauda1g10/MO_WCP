package grafo.cvrpbi.structure;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class Node {

    private int id;
    private double x;
    private double y;
    private double demand;

    public Node(int id, double x, double y, double demand) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("[").append(id).append(", ").append(x).append(", ").append(y).append(", ").append(demand).append("]");
        return stb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
