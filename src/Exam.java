import java.io.Serializable;

public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idExam;
    private String name;
    private String date;

    public Exam(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public void setId(int idExam) {
        this.idExam = idExam;
    }

    public int getId() {
        return idExam;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}
