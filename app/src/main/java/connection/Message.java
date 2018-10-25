package connection;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private String msg;

    private String id;

    private String status;

    private List<Data> data;

    public Message() {
    }

    public Message(String msg) {
        this.msg = msg;
    }

    public Message(String msg, List<Data> list) {
        this.msg = msg;
        data = list;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class Data implements Serializable {
        private String id;
        private String carNumber;
        private String name;
        private String date;
        private String status;

        public Data() {
        }

        public Data(String id, String carNumber, String name, String date, String status) {
            this.id = id;
            this.carNumber = carNumber;
            this.name = name;
            this.date = date;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCarNumber() {
            return carNumber;
        }

        public void setCarNumber(String carNumber) {
            this.carNumber = carNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
