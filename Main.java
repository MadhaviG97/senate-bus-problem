import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

import java.util.Random;

class Main{  
    public static void main(String args[]){  
        List<Rider> busQueue = new ArrayList<Rider>();
        Semaphore spaces = new Semaphore(3);
        Semaphore turnstile = new Semaphore(1);
        Semaphore mutex = new Semaphore(1);

        Bus bus = new Bus(busQueue, spaces, turnstile, mutex);
        Rider rider = new Rider(busQueue, spaces, turnstile, mutex);

        float meanBusArrivalTime = 20 * 60f * 1000 ;
        float meanRiderArrivalTime = 30f * 1000;

        EntityProducer busProducer = new EntityProducer(bus, meanBusArrivalTime, busQueue, spaces, turnstile, mutex);
        EntityProducer riderProducesr = new EntityProducer(rider, meanRiderArrivalTime, busQueue, spaces, turnstile, mutex);
        
        Thread busThread = new Thread(busProducer);
        Thread riderThread = new Thread(riderProducesr);

        busThread.start();
        riderThread.start();
    }  

}  

class EntityProducer extends Thread{
    private BusStopEntity entity;
    private float meanArrivalTime;
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;
    

    private Random randomGenerator;

    public EntityProducer(BusStopEntity entity, float meanArrivalTime, List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        
        this.entity = entity;
        this.meanArrivalTime = meanArrivalTime;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;

        randomGenerator = new Random();
    }

    @Override
    public void run() {
        System.out.println("");
        while (true){
            try{
                Thread rider = new Thread(this.entity);
                rider.start();
                Thread.sleep(nextEntityIn());
            } catch (InterruptedException e){
                System.exit(0);
            }
        }
    }

    public long nextEntityIn() {
        return Math.round(-Math.log(1 - this.randomGenerator.nextFloat()) / (1 / this.meanArrivalTime));
    }

}

interface BusStopEntity extends Runnable{
}

class Bus implements BusStopEntity{
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

class Rider implements BusStopEntity{
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