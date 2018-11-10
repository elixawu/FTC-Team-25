package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import team25core.RunToEncoderValueTask;
import opmodes.Lilac;
import team25core.ColorThiefTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Bella Heinrichs on 10/23/2018.
 */

@Autonomous(name = "Lilac Autonomous", group = "Team 25")
public class LilacAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo marker;
    private LilacAutonomous.Alliance alliance;
    private LilacAutonomous.Position position;
    //private LilacAutonomous.Side side;
    private DeadReckonPath latch;
    private DeadReckonPath detachRobot;
    private DeadReckonPath scoreMarker;
    private DeadReckonTask gold;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;

    // private Telemetry.Item allianceItem;
    // private Telemetry.Item positionItem;

    private int combo = 0;
    private int color = 0;

    private static final int SPEED_MULTIPLIER = -1;
    private int distance = 0;


    // Park combos.
    private static final int BLUE_CRATER = 0;
    private static final int RED_CRATER = 1;
    private static final int BLUE_MARKER = 2;
    private static final int RED_MARKER = 3;

    private FourWheelDirectDrivetrain drivetrain;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum Position {
        MARKER,
        CRATER,
    }


    @Override
    public void init() {
        // telemetry.setAutoClear(false);

        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        latchArm    = hardwareMap.dcMotor.get("latch");

        // Telemetry setup.
        // telemetry.setAutoClear(false);
        // allianceItem    = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        // positionItem    = telemetry.addData("POSITION", "Unselected (Y/A)");

        // Path setup.
        latch        = new DeadReckonPath();
        detachRobot  = new DeadReckonPath();
        scoreMarker  = new DeadReckonPath();

        // Single shot timer tasks for delays.
        // stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        // moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(LilacAutonomous.Alliance.BLUE);
                    // allianceItem.setValue("Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(LilacAutonomous.Alliance.RED);
                    // allianceItem.setValue("Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilacAutonomous.Position.CRATER);
                    // positionItem.setValue("Far");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(LilacAutonomous.Position.MARKER);
                    // positionItem.setValue("Near");
                    break;
                default:
                    break;
            }
        }
        setLatchPath();
        setMarkerPath();
    }

    @Override
    public void start()
    {
        doMoveToDepot();
        /*
        addTask(new SingleShotTimerTask(this, 500) {

            @Override
            public void handleEvent(RobotEvent e) {
                doLowerRobot();
            }
        });
        */
    }

    public void doMoveToDepot() {
        RobotLog.i("doMoveToDepot");
        scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,3, Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,9, -Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, -Lilac.STRAIGHT_SPEED);
        this.addTask(new DeadReckonTask(this, scoreMarker, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                //TODO: Score the lilac smelling nice marker.
                RobotLog.i("Lilac path done");
            }
        });
    }

    /* public void doLatchDetach() {
        this.addTask(new DeadReckonTask(this, detachRobot, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    doMoveToDepot();
                }
            }
        });
    }
     */

  /*  public void doLowerRobot() {
        this.addTask(new RunToEncoderValueTask(this, latchArm, 0, 1.0  ) {
            @Override
            public void handleEvent(RobotEvent e) {
                // Right now the "LATCH" path both de-latches the robot and navigates to depot
                doLatchDetach();
            }
        });
    }
*/


    private void selectAlliance(LilacAutonomous.Alliance color) {
        if (color == LilacAutonomous.Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Alliance: BLUE");
            alliance = LilacAutonomous.Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Alliance: RED");
            alliance = LilacAutonomous.Alliance.RED;
        }
    }

    public void selectPosition(LilacAutonomous.Position choice) {
        if (choice == LilacAutonomous.Position.CRATER) {
            position = LilacAutonomous.Position.CRATER;
            RobotLog.i("506 Position: FAR");
        } else {
            position = LilacAutonomous.Position.MARKER;
            RobotLog.i("506 Position: NEAR");
        }
    }

    private void setLatchPath()
    {
        latch.stop();
        /*latch.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 2, Lilac.STRAIGHT_SPEED); // Right
        latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, Lilac.STRAIGHT_SPEED * SPEED_MULTIPLIER);
        if (position == Position.MARKER) {
            latch.addSegment(DeadReckonPath.SegmentType.TURN, 135, Lilac.TURN_SPEED);
            latch.addSegment(DeadReckonPath.SegmentType.LEFT_DIAGONAL, 25, Lilac.STRAIGHT_SPEED * SPEED_MULTIPLIER);
            // Change to front right diagonal after implementing that in deadReckonPath segment types
            // Jk we need to figure out the speeds here though
            latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 72, Lilac.STRAIGHT_SPEED);*/
    }

    private void setDetachRobot()
    {
        detachRobot.stop();
        /* detachRobot.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, Lilac.SIDEWAYS_DETACH_SPEED);
        detachRobot.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, - Lilac.SIDEWAYS_DETACH_SPEED);
        detachRobot.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, - Lilac.SIDEWAYS_DETACH_SPEED);*/
    }


    private void setMarkerPath() {
        // Edit later when we figure out sensing gold block & need to implement
        // specific marker paths based off different positions of gold block.

        if (alliance == LilacAutonomous.Alliance.RED) {  // Blue and crater = 0; Red and crater = 1, Blue and marker = 2, Red and marker = 3

            color = 1;
        } else if (alliance == LilacAutonomous.Alliance.BLUE) {
            color = 0;
        }

        if (position == LilacAutonomous.Position.MARKER) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance;

        // + whichSide;

        switch (combo) {
            case BLUE_CRATER:
                RobotLog.i("506 Case: BLUE_CRATER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,3, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,9, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Lilac.STRAIGHT_SPEED);
                break;
            case RED_CRATER:
                RobotLog.i("506 Case: RED_CRATER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,3, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,9, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, -Lilac.STRAIGHT_SPEED);
                break;
            case BLUE_MARKER:
                RobotLog.i("506 Case: BLUE_MARKER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 10, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -Lilac.STRAIGHT_SPEED);
                break;
            case RED_MARKER:
                RobotLog.i("506 Case: RED_MARKER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 12, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 12, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 30, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, -Lilac.STRAIGHT_SPEED);
                break;
            default:
                break;

        }
    }
}
