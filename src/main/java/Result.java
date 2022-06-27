public class Result {
    private String property;
    private double result;

    public Result() {
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Result{" +
                "property='" + property + '\'' +
                ", result=" + result +
                '}';
    }
}
