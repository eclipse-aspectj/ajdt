public aspect AsynchronousExecutionAspect {
 
    void around() : execution(* foo()) { 
        Runnable worker = new Runnable() {
            public void run() {
                proceed();
            }
        };
        Thread asyncExecutionThread = new Thread(worker);
        asyncExecutionThread.start();
    }
}
