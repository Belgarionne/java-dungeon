package java_dungeon.objects;

import java_dungeon.map.GameMap;
import javafx.geometry.Point2D;

public class ChaseEnemy extends Enemy {
    protected final double sightDistance;
    protected Point2D targetPoint; // Current point that the AI is moving towards

    public ChaseEnemy(String name, Point2D position, String sprite, int hp, int dmg, int def, int xp) {
        super(name, position, sprite, hp, dmg, def, xp);
        this.sightDistance = 10.0;
        this.targetPoint = null;
    }

    @Override
    public void updateAI(GameMap map, Player player) {
        double distToPlayer = getPosition().distance(player.getPosition());

        // Update the enemy's target if the player is in view
        if (distToPlayer <= sightDistance && !map.linecast(getPosition(), player.getPosition())) {
            targetPoint = player.getPosition();
        }

        // Only move if the enemy has a target (or remove the target if it was reached)
        if (targetPoint == null || map.inSameTile(getPosition(), targetPoint)) {
            targetPoint = null;
            return;
        }

        // Get the direction to move towards the target
        Point2D toTarget = targetPoint.subtract(getPosition());
        Point2D moveDirection = map.getDirectionOnGrid(toTarget);

        // Try to slide around walls
        if (map.checkCollisionAt((int)(getPosition().getX() + moveDirection.getX()), (int)(getPosition().getY() + moveDirection.getY()))) {
            double dx = Math.signum(toTarget.getX());
            double dy = Math.signum(toTarget.getY());

            // Move in the opposite axis
            if (moveDirection.getX() != 0) {
                moveDirection = new Point2D(0, dy);
            }
            else if (moveDirection.getY() != 0) {
                moveDirection = new Point2D(dx, 0);
            }
        }

        // Get the new position on the grid (make sure the position is locked to a integer grid)
        int newX = (int)(getPosition().getX() + moveDirection.getX());
        int newY = (int)(getPosition().getY() + moveDirection.getY());
        Point2D newPos = new Point2D(newX, newY);

        // Check for combat
        if (map.inSameTile(getPosition(), player.getPosition()) || map.inSameTile(newPos, player.getPosition())) {
            attack(player);
        }
        // Check for collision
        else if (!map.checkCollisionAt(newX, newY)) {
            move(moveDirection);
        }
    }
}
