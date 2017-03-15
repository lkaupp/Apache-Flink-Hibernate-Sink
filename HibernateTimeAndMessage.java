import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Lukas on 13.03.2017.
 */
@Entity
@Table(indexes = {
        @Index(columnList = "id", name = "id_hidx"),
        @Index(columnList = "date", name = "date_hidx")
})
public class HibernateTimeAndMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Date date;

    private String path;

    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
