/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;

// import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;   
  
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.TimedRobot;
// import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.GripPipeline;

import frc.robot.subsystems.*;

import java.lang.Thread;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  public static Drivetrain drivetrain;
  public static CargoIntake cargoIntake;
  public static HatchLatch hatchLatch;

  public static NetworkTableEntry arcadeDrive;
  public static NetworkTableEntry hatchExtend;
  public static NetworkTableEntry hatchOpen;
  public static NetworkTableEntry cXEntry;

  public static CameraServer inst;
  public static MjpegServer server;
  public static UsbCamera usobo1;
  public static UsbCamera usobo2;

  public static GripPipeline pipeline;
  public static Thread thread;

  private static final int IMG_WIDTH = 320;
  private static final int IMG_HEIGHT = 240;
  public static double centerX = 0.0;
  private final Object imgLock = new Object();

// always add last
  public static OI m_oi;


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {

    drivetrain = new Drivetrain();
    cargoIntake = new CargoIntake();
    hatchLatch = new HatchLatch();


    ShuffleboardTab shuffTab = Shuffleboard.getTab("Drive");

    hatchOpen = shuffTab
    .add("HatchOpen", false) 
    .withWidget(BuiltInWidgets.kBooleanBox)
    .withPosition(0, 1)
    .getEntry();

  hatchExtend = shuffTab
    .add("HatchExtend", false) 
    .withWidget(BuiltInWidgets.kBooleanBox)
    .withPosition(0, 0)
    .getEntry();  

    CameraServer inst = CameraServer.getInstance();

    usobo1 = new UsbCamera("Forward Cam", 0);
    usobo1.setResolution(IMG_WIDTH, IMG_HEIGHT);
    usobo1.setExposureAuto();
    inst.addCamera(usobo1);
    usobo2 = new UsbCamera("Other Cam", 1);
    usobo2.setExposureManual(0);
    inst.addCamera(usobo2);

    server = inst.addServer("serve_USB Camera 0");
    server.setSource(usobo1);
    server.setCompression(-1);
   
    shuffTab
    .add("Forward Cam", usobo1)
    .withWidget(BuiltInWidgets.kCameraStream)
    .withPosition(1, 0)
    .withSize(5, 4);  

  cXEntry = shuffTab
    .add("Center X", centerX)
    .withWidget(BuiltInWidgets.kNumberBar)
    .getEntry();

    thread = new Thread(() -> {
      CvSink cvSink = inst.getVideo();

      Mat source = new Mat();

      while(!Thread.interrupted()){
        cvSink.grabFrame(source);

        pipeline.process(source);

        if (!pipeline.findContoursOutput().isEmpty()) {
          Rect r = Imgproc.boundingRect(pipeline.findContoursOutput().get(0));

          synchronized (imgLock) {
            Robot.centerX = r.x + (r.width /2);
            Robot.cXEntry.setNumber(Robot.centerX);
          }
        }
      }
  });

  m_oi = new OI();
  }
  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   * You can use it to reset any subsystem information you want to clear when
   * the robot is disabled.
   */
  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString code to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional commands to the
   * chooser code above (like the commented example) or additional comparisons
   * to the switch structure below with additional strings & commands.
   */
  @Override
  public void autonomousInit() {

    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector",
     * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
     * = new MyAutoCommand(); break; case "Default Auto": default:
     * autonomousCommand = new ExampleCommand(); break; }
     */

    // schedule the autonomous command (example)
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    Scheduler.getInstance().run();
  }

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    Scheduler.getInstance().run();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
