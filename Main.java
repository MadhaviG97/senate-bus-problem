import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
  
class Main{  
    public static void main(String args[]){  
        List<Rider> busQueue = new ArrayList<Rider>();
        Semaphore spaces = new Semaphore(3);
        Semaphore turnstile = new Semaphore(1);
        Semaphore mutex = new Semaphore(1);
        Bus bus = new Bus(busQueue, spaces, turnstile, mutex);
        Rider rider = new Rider(busQueue, spaces, turnstile, mutex);

        Thread threadBus1 = new Thread(bus);
        Thread threadBus2 = new Thread(bus);
        Thread threadBus3 = new Thread(bus);
        Thread thread1 = new Thread(rider);
        Thread thread2 = new Thread(rider);
        Thread thread3 = new Thread(rider);
        Thread thread4 = new Thread(rider);
        Thread thread5 = new Thread(rider);

        threadBus1.start();

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();

        threadBus2.start();
        threadBus3.start();
    }  

}  

class Bus extends Thread{
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;

    public Bus(List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;
    }

    public void run() {
        System.out.println("Thread Bus started");
        
        try{
            this.turnstile.acquire();
            // CRITICAL SECTION
            this.mutex.acquire();

            Iterator<Rider> iterator = busQueue.iterator();
            int count=0;
            while (iterator.hasNext()) {
                Rider nextRider = iterator.next();
                iterator.remove();
                nextRider.boardBus(this);
                this.spaces.release();
                count++;
            }
            this.depart(count);
            this.mutex.release();
            // CRITICAL SECTION END 
            
            this.turnstile.release();
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    public void depart(int count){
        System.out.println("Bus is departing with " + count + " riders");
    }
}

class Rider extends Thread{
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;

    public Rider(List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;
    }

    public void run() {
        System.out.println("Thread Rider started");
        try{
            turnstile.acquire();
            turnstile.release();
    
            spaces.acquire();

            // CRITICAL SECTION
            mutex.acquire();
            busQueue.add(this);
            mutex.release(); 
            // CRITICAL SECTION END   

            System.out.println("Thread Added to the List");     
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    public void boardBus(Bus bus){
        System.out.println("Rider boarded...");
    }
}