package ajia.tracking;


public aspect TimeTracker {
     private static interface LastAccessedTimeHolder {
         static aspect Impl {
             private long LastAccessedTimeHolder.lastAccessedTime;

             public long LastAccessedTimeHolder.getLastAccessedTime() {
                 return lastAccessedTime;
             }

             public void
                 LastAccessedTimeHolder.setLastAccessedTime(long time) {
                 lastAccessedTime = time;
             }
         }
     }

     declare parents : @Service * implements LastAccessedTimeHolder;

     before(LastAccessedTimeHolder service)
         : execution(* LastAccessedTimeHolder+.*(..)) && this(service)
           && !within(TimeTracker) {
         service.setLastAccessedTime(System.nanoTime());
     }
}

@interface Service { }
