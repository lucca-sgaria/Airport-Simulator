package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Airport extends Thread {
    List<Runway> runwaysLanding = new ArrayList<>();
    List<Runway> runwaysTakeoff = new ArrayList<>();
    List<Airway> airways = new ArrayList<>();
    List<Plane> planes = new ArrayList<>();
    Hangar hangar = new Hangar(this);

    Semaphore semaphoreAirwayHighest = new Semaphore(1);

    @Override
    public void run() {
        while (true) {


            System.out.println("===============================================================================================");
            System.out.println("                                    Airport                                                    ");

            System.out.println(airways.get(2).toPrintString());
            System.out.println(airways.get(1).toPrintString());
            System.out.println(airways.get(0).toPrintString());

            System.out.println("--------");
            System.out.println(runwaysLanding.get(0).toPrintString());
            System.out.println("--------");
            System.out.println(hangar.toPrintString());
            System.out.println("--------");
            System.out.println(runwaysTakeoff.get(0).toPrintString());

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRunwayLanding(Runway runway) {
        this.runwaysLanding.add(runway);
    }

    public void addRunwayTakeoff(Runway runway) {
        this.runwaysTakeoff.add(runway);
    }

    public void addAirway(Airway airway) {
        this.airways.add(airway);
    }

    public void addPlane(Plane plane) {
        this.planes.add(plane);
    }

    public void startPlanes() {
        planes.forEach(Thread::start);
    }

    public long sendToHighestAirway(Plane plane) {
        Airway next = this.airways
                .stream()
                .max(Comparator.comparing(Airway::getHeight))
                .orElse(null);

        try {
            semaphoreAirwayHighest.acquire();
            next.addPlane(plane);
            semaphoreAirwayHighest.release();
        }catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return next.getFlighTime();
    }

    public long sendToLowerAirway(Plane plane) {
        Airway airway = findPlaneInAirway(plane);

        Airway nextAirway = this.airways
                .stream()
                .filter(way -> way.getHeight() < airway.getHeight())
                .max(Comparator.comparing(Airway::getHeight))
                .orElse(null);

        try {
            nextAirway.getSemaphore().acquire();
            nextAirway.addPlane(plane);
            nextAirway.getSemaphore().release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        airway.removePlane(plane);


        return nextAirway.getFlighTime();
    }

    public long sendToLandRunway(Plane plane) {
        Airway airway = findPlaneInAirway(plane);

        Runway runway = runwaysLanding.get(0);

        try {
            runway.getSemaphore().acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        runway.addPlane(plane);
        airway.removePlane(plane);

        return runway.getUsageTime();
    }

    public void sendToHangar(Plane plane) {

        this.runwaysLanding.get(0).removePlane(plane);
        this.runwaysLanding.get(0).getSemaphore().release();

        this.hangar.addPlane(plane);
    }

    public long sendToAirway(Plane plane) {
        runwaysTakeoff.get(0).removePlane(plane);
        runwaysTakeoff.get(0).getSemaphore().release();
        return sendToHighestAirway(plane);
    }


    public boolean hasNextAirway(Plane plane) {
        Airway airway = findPlaneInAirway(plane);

        long count = this.airways
                .stream()
                .filter(way -> way.getHeight() < airway.getHeight())
                .count();

        return count > 0;
    }

    private Airway findPlaneInAirway(Plane plane) {
        Airway airway = this.airways
                .stream()
                .filter(way -> way.getPlanes().contains(plane))
                .findFirst().orElse(null);

        return airway;
    }

    public long sendToTakeOffRunway(Plane plane) {
        hangar.getPlanes().remove(plane);

        Runway runway = runwaysTakeoff.get(0);
        runway.addPlane(plane);

        return runway.getUsageTime();
    }

    public List<Plane> getPlanes() {
        return planes;
    }

    public void setPlanes(List<Plane> planes) {
        this.planes = planes;
    }

    public List<Runway> getRunwaysLanding() {
        return runwaysLanding;
    }

    public void setRunwaysLanding(List<Runway> runwaysLanding) {
        this.runwaysLanding = runwaysLanding;
    }

    public List<Runway> getRunwaysTakeoff() {
        return runwaysTakeoff;
    }

    public void setRunwaysTakeoff(List<Runway> runwaysTakeoff) {
        this.runwaysTakeoff = runwaysTakeoff;
    }

    public List<Airway> getAirways() {
        return airways;
    }

    public void setAirways(List<Airway> airways) {
        this.airways = airways;
    }

    public Hangar getHangar() {
        return hangar;
    }

    public void setHangar(Hangar hangar) {
        this.hangar = hangar;
    }

    @Override
    public String toString() {
        return "model.Airport{" +
                "runwaysLanding=" + runwaysLanding +
                ", runwaysTakeoff=" + runwaysTakeoff +
                ", airways=" + airways +
                ", planes=" + planes +
                ", hangar=" + hangar +
                '}';
    }


}
