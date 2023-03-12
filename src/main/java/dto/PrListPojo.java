package dto;

public class PrListPojo {

    PRPojo[] prPojos;

    public PrListPojo(PRPojo[] prPojos) {
        this.prPojos = prPojos;
    }

    public PrListPojo() {
    }

    public PRPojo[] getPrPojos() {
        return prPojos;
    }

    public void setPrPojos(PRPojo[] prPojos) {
        this.prPojos = prPojos;
    }
}
