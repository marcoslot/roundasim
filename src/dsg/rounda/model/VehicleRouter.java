/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.model.VehicleRouter.Tracer;
import dsg.rounda.services.roadmap.TrackArea1D;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;


/**
 * Computes how vehicles move over the road network
 */
public class VehicleRouter {

    final TrackProvider roadNetwork;
    
    public VehicleRouter(TrackProvider roadNetwork) {
        this.roadNetwork = roadNetwork;
    }
    
    public interface Condition {
        boolean isTrue();
    }

    protected Position1D route(
            VehicleState vehicle, 
            double distanceToDrive) {
        
        return route(
                vehicle.getBackPosition(), 
                vehicle.getTrajectory(), 
                distanceToDrive
        );
    }

    public Position1D route(
            Position1D vehiclePosition, 
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive) {
        
        return route(
                vehiclePosition,
                vehicleTrajectory,
                distanceToDrive,
                new NoopTracer()
        );
    }
    
    public Position1D route(
            Position1D vehiclePosition, 
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive,
            Tracer tracer) {
        
        Track currentTrack = vehiclePosition.getTrack();
        double currentOffset = vehiclePosition.getOffset();
        
        tracer.startOnTrack(currentTrack.getId(), currentOffset);

        while(distanceToDrive >= 0) {
            double endOfCurrentTrack = currentTrack.getPathLength();
            Track nextTrack = vehicleTrajectory.getNextTrack();
            
            if(nextTrack != null) {
                Connector from = nextTrack.getFrom();
                
                if(from.getRoad() == currentTrack.getId()
                && from.getOffset() >= currentOffset) {
                    // The next track starts on this road, directly ahead of us
                    endOfCurrentTrack = from.getOffset();
                } else {
                    // The next track is not directly ahead of us, ignore
                    nextTrack = null;
                }
            }
            
            double remainingOnTrack = endOfCurrentTrack - currentOffset;

            if(remainingOnTrack > distanceToDrive) {
                // Move forward on track
                currentOffset += distanceToDrive;

                tracer.endOnTrack(currentTrack.getId(), currentOffset, false);
                break;
            } else if(nextTrack != null) {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);
                
                // Move into new track from trajectory
                vehicleTrajectory.popNextTrack();
                currentOffset = 0;
                currentTrack = nextTrack;
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            } else {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);

                if(currentTrack.getTo() == null) {
                    // We've reached the end of the track
                    // We will now part this world
                    return new Position1D(null, 0);
                }
                
                // Arrived at the end of the track
                // Move into connected road
                currentOffset = currentTrack.getTo().getOffset();
                currentTrack = roadNetwork.getRoad(currentTrack.getTo().getRoad());

                if(currentTrack == null) {
                    // The  connector of this track points to a
                    // a track that does not exist, bail
                    return new Position1D(currentTrack, currentOffset);
                }
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            }
            
            distanceToDrive -= remainingOnTrack;
        }
        
        return new Position1D(currentTrack, currentOffset);
    }

    public Position1D route(
            Position1D vehiclePosition, 
            Trajectory1D vehicleTrajectory, 
            TrackMapArea1D area,
            Tracer tracer) {
        
        Track currentTrack = vehiclePosition.getTrack();
        double currentOffset = vehiclePosition.getOffset();
        
        tracer.startOnTrack(currentTrack.getId(), currentOffset);

        while(true) {
            double endOfCurrentTrack = currentTrack.getPathLength();
            Track nextTrack = vehicleTrajectory.getNextTrack();
            
            if(nextTrack != null) {
                Connector from = nextTrack.getFrom();
                
                if(from.getRoad() == currentTrack.getId()
                && from.getOffset() >= currentOffset) {
                    // The next track starts on this road, directly ahead of us
                    endOfCurrentTrack = from.getOffset();
                } else {
                    // The next track is not directly ahead of us, ignore
                    nextTrack = null;
                }
            }
            
            TrackArea1D boundArea = area.getArea(currentTrack.getId());
            
            if(boundArea == null) {
                // not in the area
                break;
            }
            
            TrackRange1D boundRange = boundArea.getRangeContaining(currentOffset);
            
            if(boundRange == null) {
                // not in the area
                break;
            }
            
            double distanceToDrive = boundRange.getEnd() - currentOffset;
            double remainingOnTrack = endOfCurrentTrack - currentOffset;

            if(remainingOnTrack > distanceToDrive + 0.00001) {
                // Move forward on track
                currentOffset += distanceToDrive;

                tracer.endOnTrack(currentTrack.getId(), currentOffset, false);
                break;
            } else if(nextTrack != null) {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);
                
                // Move into new track from trajectory
                vehicleTrajectory.popNextTrack();
                currentOffset = 0;
                currentTrack = nextTrack;
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            } else {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);

                if(currentTrack.getTo() == null) {
                    // We've reached the end of the track
                    // We will now part this world
                    return new Position1D(null, 0);
                }
                
                // Arrived at the end of the track
                // Move into connected road
                currentOffset = currentTrack.getTo().getOffset();
                currentTrack = roadNetwork.getRoad(currentTrack.getTo().getRoad());

                if(currentTrack == null) {
                    // The  connector of this track points to a
                    // a track that does not exist, bail
                    return new Position1D(currentTrack, currentOffset);
                }
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            }
        }
        
        return new Position1D(currentTrack, currentOffset);
    }
    
    public interface Tracer {
        void startOnTrack(int trackID, double startOffset);
        public void endOnTrack(int trackID, double endOffset, boolean trackChange);
    }
    
    public static class NoopTracer implements Tracer {
        @Override
        public void startOnTrack(int track, double offset) {
            
        }
        @Override
        public void endOnTrack(int track, double offset, boolean trackChange) {
            
        }
    }

    public Position1D routeUntil(
            Position1D vehiclePosition, 
            Trajectory1D vehicleTrajectory, 
            TrackMapArea1D areaNotToDrive, 
            Tracer tracer) {
        
        Track currentTrack = vehiclePosition.getTrack();
        double currentOffset = vehiclePosition.getOffset();
        
        tracer.startOnTrack(currentTrack.getId(), currentOffset);

        while(true) {
            double endOfCurrentTrack = currentTrack.getPathLength();
            Track nextTrack = vehicleTrajectory.getNextTrack();
            
            if(nextTrack != null) {
                Connector from = nextTrack.getFrom();
                
                if(from.getRoad() == currentTrack.getId()
                && from.getOffset() >= currentOffset) {
                    // The next track starts on this road, directly ahead of us
                    endOfCurrentTrack = from.getOffset();
                } else {
                    // The next track is not directly ahead of us, ignore
                    nextTrack = null;
                }
            }

            // Normally, drive all the way to the end
            double distanceToDrive = endOfCurrentTrack - currentOffset;
            
            TrackArea1D boundArea = areaNotToDrive.getArea(currentTrack.getId());
            
            if(boundArea != null) {
                // We can only move forward, find the first range that is
                // ahead of the current offset, if any
                TrackRange1D criticalRange = boundArea.getRangeAfter(currentOffset);

                if(criticalRange != null) {
                    if(criticalRange.contains(currentOffset)) {
                        // entered into the area, we're done
                        break;
                    }
                    if(criticalRange.getStart() < endOfCurrentTrack) {
                        // We will drive up to the start of the range and stop
                        distanceToDrive = criticalRange.getStart() - currentOffset;
                    }
                }
            }

            double remainingOnTrack = endOfCurrentTrack - currentOffset;

            if(remainingOnTrack > distanceToDrive) {
                // Move forward on track
                currentOffset += distanceToDrive;

                tracer.endOnTrack(currentTrack.getId(), currentOffset, false);
                break;
            } else if(nextTrack != null) {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);
                
                // Move into new track from trajectory
                vehicleTrajectory.popNextTrack();
                currentOffset = 0;
                currentTrack = nextTrack;
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            } else {
                tracer.endOnTrack(currentTrack.getId(), endOfCurrentTrack, true);

                if(currentTrack.getTo() == null) {
                    // We've reached the end of the track
                    // We will now part this world
                    return new Position1D(null, 0);
                }
                
                // Arrived at the end of the track
                // Move into connected road
                currentOffset = currentTrack.getTo().getOffset();
                currentTrack = roadNetwork.getRoad(currentTrack.getTo().getRoad());

                if(currentTrack == null) {
                    // The  connector of this track points to a
                    // a track that does not exist, bail
                    return new Position1D(currentTrack, currentOffset);
                }
                
                tracer.startOnTrack(currentTrack.getId(), currentOffset);
            }
        }
        
        return new Position1D(currentTrack, currentOffset);
    }
}
