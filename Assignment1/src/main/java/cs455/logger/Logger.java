package cs455.logger;

public  class Logger {
	public static boolean printStackTrace=false;
	
	public static synchronized void write_errors(String className,String method,String what,  Exception stackTrace) {
		System.out.println("EXCEPTION:");
		System.out.println("\tIn Method: "+className);
		System.out.println("\tIn Method: "+method);
		System.out.println("\tTYPE: "+what);
		if(printStackTrace==true) {
			System.out.println("\tStackTrace:");
			stackTrace.printStackTrace();
		}
	}
}
