package ajia;

public interface Nameable {
    public void setName(String name);
    public String getName();

    static aspect Impl {
        private String Nameable.name;

        public void Nameable.setName(String name) {
            this.name = name;
        }

        public String Nameable.getName() {
            return this.name;
        }
    } 
} 