import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

import java.util.Random;

class Main{  

    private static final int N = 50; 

    public static void main(String args[]){ 
         
        List<Rider> busQueue = new ArrayList<Rider>();
        Semaphore spaces = new Semaphore(N);    // remaining slots at the bus stop
        Semaphore no_bus = new Semaphore(1);    // to indicate if a bus has arrived at the present or not
        Semaphore mutex = new Semaphore(1);

        float meanBusArrivalTime = 20 * 60f * 1000 ;
        float meanRiderArrivalTime = 0.5 * 60f * 1000;

        // test
        // float meanBusArrivalTime = 3f * 1000 ;
        // float meanRiderArrivalTime = 1f * 1000;

        EntityProducer busProducer = new EntityProducer("bus", meanBusArrivalTime, busQueue, spaces, no_bus, mutex);
        EntityProducer riderProducesr = new EntityProducer("rider", meanRiderArrivalTime, busQueue, spaces, no_bus, mutex);
        
        Thread busThread = new Thread(busProducer);
        Thread riderThread = new Thread(riderProducesr);

        busThread.start();
        riderThread.start();
    }  

}  

// Entity producer class controls the creation of Bus and Rider threads according to the given probability distributions
class EntityProducer extends Thread{
    private Runnable entity;
    private String entityType;
    private float meanArrivalTime;
    private Random randomGenerator;
    private List<Rider> busQueue;
    private Semaphore spaces;
    private Semaphore no_bus;
    private Semaphore mutex;


    public EntityProducer(String entityType, float meanArrivalTime, List<Rider> busQueue, Semaphore spaces, Semaphore no_bus, Semaphore mutex){
        super();
        
        this.entityType = entityType;
        this.meanArrivalTime = meanArrivalTime;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.no_bus = no_bus;
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
                    entity = new Bus(count, busQueue, spaces, no_bus, mutex);
                } else{
                    entity = new Rider(count, busQueue, spaces, no_bus, mutex);
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
    private Semaphore no_bus;
    private Semaphore mutex;

    public Bus(int id, List<Rider> busQueue, Semaphore spaces, Semaphore no_bus, Semaphore mutex){
        super();
        this.id = id;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.no_bus = no_bus;
        this.mutex = mutex;
    }

    public void run() {
        System.out.println("Bus " + this.id +" arrived...");
        
        try{
            this.no_bus.acquire();

            int count=0;
            this.mutex.acquire();

            // CRITICAL SECTION BEGIN
            
            // board all riders at the bus stop
            Iterator<Rider> iterator = busQueue.iterator();
            while (iterator.hasNext()) {
                Rider nextRider = iterator.next();
                iterator.remove();
                nextRider.boardBus(this);
                this.spaces.release();
                count++;
            }
            // CRITICAL SECTION END 

            this.mutex.release();
            this.depart(count);
            
            this.no_bus.release();
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
    private Semaphore no_bus;
    private Semaphore mutex;

    public Rider(int id, List<Rider> busQueue, Semaphore spaces, Semaphore no_bus, Semaphore mutex){
        super();
        this.id = id;
        this.busQueue = busQueue;
        this.spaces = spaces;
        this.no_bus = no_bus;
        this.mutex = mutex;
    }
    
    public void run() {
        System.out.println("Rider "+ this.id +" arrived");
        try{
            // If a bus has aarived at the present moment and boarding passengers at the bus stop,
            // then avoid the rider boarding the bus
            no_bus.acquire();
            no_bus.release();
    
            spaces.acquire();

            mutex.acquire();

            // CRITICAL SECTION BEGIN

            // rider is in the bus stop
            busQueue.add(this);

            // CRITICAL SECTION END   

            mutex.release(); 

            System.out.println("Rider " + this.id + " is in the bus stop");     
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    public void boardBus(Bus bus){
        System.out.println("Rider " + this.id + " boarded");
    }
}