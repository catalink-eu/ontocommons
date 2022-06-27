import java.util.ArrayList;

public class Output {
    private String timestamp, source;
    private ArrayList<Result> results;

    public Output() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "Output{" +
                "timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                ", results=" + results +
                '}';
    }
}
