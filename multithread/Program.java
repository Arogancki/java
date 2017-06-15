class Producer implements Runnable
{
	PrintQueue q;
	Producer( PrintQueue aQueue )
	{
		q = aQueue;
		Thread t = new Thread( this, "Producer" );
		t.start( );
	}
	public void run( )
	{
		for( int i = 0; i < 10; i++ ) {
			q.put( i );
		}
	}
	}
	class Consumer implements Runnable
	{
		PrintQueue q;
		Consumer( PrintQueue aQueue )
		{
			q = aQueue;
			Thread t = new Thread( this, "Consumer" );
			t.start( );
		}
		public void run( )
		{
			for( int i = 0; i < 10; i++ ) {
				q.get( );
			}
		}
	}
	public class Program
	{
		public static void main( String[ ] args )
		{
			PrintQueue q = new PrintQueue( );
			new Producer( q );
			new Consumer( q );
		}
		
	}
	
	class PrintQueue {
		private int value;
		private boolean valueSet = false;
		public synchronized int get( ) {
			while( !valueSet ) {
				try {
					wait( ); //zwalnia monitor
				}
					catch( InterruptedException e ) {}
				}
				System.out.println( "Got: " + value );
				valueSet = false;
				notifyAll( );
				return value;
			} //monitor jest zwalniany
			public synchronized void put( int aValue ) {
				while( valueSet ) {
					try {
						wait( ); //zwalnia monitor
					}
					catch( InterruptedException e ) {}
				}
				value = aValue;
				System.out.println( "Put: " + value );
				valueSet = true;
				notifyAll( );
			} //monitor jest zwalniany
	}
