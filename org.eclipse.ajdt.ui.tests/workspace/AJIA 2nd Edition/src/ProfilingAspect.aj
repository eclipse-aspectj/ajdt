public aspect ProfilingAspect {
        private long AccessTracked.lastAccessedTime;

        public void AccessTracked.updateLastAccessedTime() {
            lastAccessedTime = System.nanoTime();
        }

        public long AccessTracked.getLastAccessedTime() {
            return lastAccessedTime;
        }

        before(AccessTracked accessTracked)
    : execution(* AccessTracked+.*(..)) 
      && !execution(* AccessTracked.*(..)) 
      && this(accessTracked){
    accessTracked.updateLastAccessedTime(); // ERROR reported here
}    

}
  
      
interface AccessTracked { }
