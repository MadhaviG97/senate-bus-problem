import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

import java.util.Random;

class Main{  
    public static void main(String args[]){  
        List<Rider> busQueue = new ArrayList<Rider>();
        Semaphore spaces = new Semaphore(50);
        Semaphore turnstile = new Semaphore(1);
        Semaphore mutex = new Semaphore(1);

        float meanBusArrivalTime = 20 * 60f * 1000 ;
        float meanRiderArrivalTime = 30f * 1000;

        // test
        // float meanBusArrivalTime = 3f * 1000 ;
        // float meanRiderArrivalTime = 1f * 1000;

        EntityProducer busProducer = new EntityProducer("bus", meanBusArrivalTime, busQueue, spaces, turnstile, mutex);
        EntityProducer riderProducesr = new EntityProducer("rider", meanRiderArrivalTime, busQueue, spaces, turnstile, mutex);
        
        Thread busThread = new Thread(busProducer);
        Thread riderThread = new Thread(riderProducesr);

        busThread.start();
        riderThread.start();
    }  

}  

class EntityProducer extends Thread{
    private Runnable entity;
    private String entityType;
    private float meanArrivalTime;
    private Random randomGenerator;
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;


    public EntityProducer(String entityType, float meanArrivalTime, List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        
        this.entityType = entityType;
        this.meanArrivalTime = meanArrivalTime;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;

        randomGenerator = new Random();
    }

    @Override
    public void run() {        
        int count=0;
        while (true){
            try{
                count++;
                if (entityType=="bus"){
                    entity = new Bus(count, busQueue, spaces, turnstile, mutex);
                } else{
                    entity = new Rider(count, busQueue, spaces, turnstile, mutex);
                }
                Thread rider = new Thread(entity);
                
                System.out.println("Produced "+ entity.getClass().getName() + " " + count);

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

class Bus implements Runnable{
    private int id;
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;

    public Bus(int id, List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        this.id = id;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;
    }

    public void run() {
        System.out.println("Bus " + this.id +" arrived...");
        
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
        System.out.println("Bus " + this.id + " is departing with " + count + " riders");
    }
}

class Rider implements Runnable{
    private int id;
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore turnstile;
    private Semaphore mutex;

    public Rider(int id, List<Rider> busQueue, Semaphore spaces, Semaphore turnstile, Semaphore mutex){
        super();
        this.id = id;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.turnstile = turnstile;
        this.mutex = mutex;
    }
    
    public void run() {
        System.out.println("Rider "+ this.id +" arrived");
        try{
            turnstile.acquire();
            turnstile.release();
    
            spaces.acquire();

            // CRITICAL SECTION
            mutex.acquire();
            busQueue.add(this);
            mutex.release(); 
            // CRITICAL SECTION END   

            System.out.println("Rider " + this.id + " is in the busStop");     
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    public void boardBus(Bus bus){
        System.out.println("Rider " + this.id + " boarded");
    }
}