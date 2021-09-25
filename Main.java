import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

enum qualified {
    READY,
    WAIT
}

  
class Main{  
    public static void main(String args[]){  
        BusStop busStop = new BusStop(0);
        Bus bus = new Bus();
        Rider rider1 = new Rider(0);
        Rider rider2 = new Rider(0);
        Rider rider3 = new Rider(0);
        Rider rider4 = new Rider(0);

        busStop.riderArrives(rider1);
        busStop.riderArrives(rider2);
        busStop.riderArrives(rider3);

        busStop.busArrives(bus);

        busStop.riderArrives(rider4);

    }  
}  

class BusStop{
    private Queue<Rider> queue;
    private int state;

    public BusStop(int state){
        this.state = state;
        queue = new LinkedList<>();
    }

    public int getState(){
        return this.state;
    }

    private void setState(int state){
        this.state = state;
    }

    public void riderArrives(Rider rider){
        if (this.state==1){
            rider.waitForNextBus();
        }
        queue.add(rider);
        System.out.println("Rider is in the queue...");
    }

    public void busArrives(Bus bus){
        System.out.println("Bus has arrived...");
        this.setState(1);

        boolean isEmpty = this.queue.isEmpty();
        if (!isEmpty){
            this.boardRiders();
        }
        bus.depart();
        this.setState(0);
        this.getReadyRiders();
    }

    private void getReadyRiders(){
        Iterator<Rider> iterator = queue.iterator();

        while (iterator.hasNext()) {
            Rider nextRider = iterator.next();

            if (nextRider.getStatus() == 0)
                continue;

            nextRider.getReadyForNextBus();
        }
    }

    private void boardRiders(){
        Iterator<Rider> iterator = queue.iterator();
        Integer count = 0;

        while (iterator.hasNext() && count<50 && iterator.next().getStatus()!=1) {
            iterator.remove();
            count++;
        }
    }
}

class Bus extends Thread{
    public Bus(){
        run();
    }

    public void run() {
        System.out.println("Thread Bus started");
    }

    public void depart(){
        System.out.println("Bus is departing...");
    }
}

class Rider extends Thread{
    private int status;

    public Rider(int status){
        super();
        this.status = status;
        run();
    }

    public void run() {
        System.out.println("Thread Rider started");
    }

    public int getStatus(){
        return this.status;
    }

    public void boardBus(Bus bus){
        System.out.println("Rider boarded...");
    }

    public void waitForNextBus(){
        this.status = 1;
        System.out.println("Wait for the next Bus...");
    }

    public void getReadyForNextBus(){
        this.status = 0;
        System.out.println("Ready for the next Bus...");
    }
}