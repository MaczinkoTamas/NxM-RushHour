package nye.progtech.model.map;

import java.util.Objects;

public record TargetGoal(
        TargetOwner owner,
        String vehicleId,
        ExitSide exitSide,
        GridCoordinate exitCoordinate
) {
    public TargetGoal {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(vehicleId, "vehicleId");
        Objects.requireNonNull(exitSide, "exitSide");
        Objects.requireNonNull(exitCoordinate, "exitCoordinate");
    }
}
