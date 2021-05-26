package com.flipkart.service;

import com.flipkart.bean.Course;
import com.flipkart.bean.Professor;
import com.flipkart.bean.Student;
import com.flipkart.constant.SQLQueries;
import com.flipkart.dao.AdminDaoInterfaceImpl;
import com.flipkart.dao.NotificationDaoOperation;
import com.flipkart.exception.*;
import com.flipkart.utils.DBUtil;
import com.flipkart.dao.RegistrationDaoInterface;
import com.flipkart.dao.RegistrationDaoOperation;
import com.flipkart.dao.UserDAOOperation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminOperation implements AdminInterface{

    private static volatile AdminOperation instance = null;

    private AdminOperation(){}

    public static AdminOperation getInstance() {
        if (instance == null) {
            synchronized (AdminOperation.class) {
                instance = new AdminOperation();
            }
        }
        return instance;
    }

    AdminDaoInterfaceImpl adminDaoInterfaceImpl = AdminDaoInterfaceImpl.getInstance();
    NotificationDaoOperation notificationDao = NotificationDaoOperation.getInstance();
    @Override
    public void addCourse(Course course) throws CourseFoundException {
        try{
            adminDaoInterfaceImpl.addCourse(course);
        } catch (CourseFoundException e) {
            throw e;
        }
    }

    @Override
    public void removeCourse(int courseID) throws CourseNotFoundException, CourseNotDeletedException {
        try{
            adminDaoInterfaceImpl.removeCourse(courseID);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void addProf(Professor professor) throws ProfNotAddedException, ProfFoundException{
        try{
            adminDaoInterfaceImpl.addProf(professor);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void removeProf(String profID) throws ProfNotFoundException, ProfNotDeletedException {
        try {
            adminDaoInterfaceImpl.removeProf(profID);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void removeStudent(String studentID) throws StudentNotFoundException {
        try{
            adminDaoInterfaceImpl.removeStudent(studentID);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void assignProf(String profID, int courseID) throws CourseNotFoundException, ProfNotFoundException{
        try{
            adminDaoInterfaceImpl.assignProf(profID, courseID);
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Method to ensure no course has less than three registered students
     */
    @Override
    public void validateRegistration() throws DatabaseException {
    	try {
    		HashMap<String,List<Integer>> alterCourses = adminDaoInterfaceImpl.getAlternateCourses();
        	HashMap<String,List<Integer>> prefCourses = adminDaoInterfaceImpl.getPreferredCourses();
        	HashMap<String,Integer> coursesNeeded = new HashMap<>();
        	
        	for (HashMap.Entry mapElement :prefCourses.entrySet()) {
                String studentID = (String)mapElement.getKey();
                List<Integer> courses = (List<Integer>)mapElement.getValue();
                for(Integer courseCode:courses) {
            		Course course = RegistrationDaoOperation.getInstance().getCourse(courseCode);
            		if(course.getFilledSeats()<Course.MAX_SEATS){
            			if(coursesNeeded.containsKey(studentID)) {
            				coursesNeeded.put(studentID, coursesNeeded.get(studentID)-1);
            			}
            			else {
            				coursesNeeded.put(studentID, 3);
            			}
            			RegistrationDaoOperation.getInstance().addCourse(studentID, courseCode);
            		}
                }
        	}
        	
        	for (HashMap.Entry mapElement :alterCourses.entrySet()) {
                String studentID = (String)mapElement.getKey();
                List<Integer> courses = (List<Integer>)mapElement.getValue();
                for(Integer courseCode:courses) {
            		Course course = RegistrationDaoOperation.getInstance().getCourse(courseCode);
            		if(course.getFilledSeats()<Course.MAX_SEATS){
            			if(coursesNeeded.get(studentID)==null) {
                			RegistrationDaoOperation.getInstance().addCourse(studentID, courseCode);
            			}
            			else if(coursesNeeded.get(studentID)!=0) {
            				if(coursesNeeded.containsKey(studentID)) {
                				coursesNeeded.put(studentID, coursesNeeded.get(studentID)-1);
                			}
                			else {
                				coursesNeeded.put(studentID, 3);
                			}
                			RegistrationDaoOperation.getInstance().addCourse(studentID, courseCode);
            			}
            		}
                }
        	}
        	
        	adminDaoInterfaceImpl.validateRegistration();
    	}
    	catch(Exception e) {
    		throw new DatabaseException();
    	}
		
    }

    @Override
    public void generateReportCard(String studentID) throws StudentNotFoundException {
        try {
            adminDaoInterfaceImpl.generateReportCard(studentID);
        } catch (StudentNotFoundException e) {
            throw e;
        }
    }

    /**
     * Method to get list of courses in catalog
     * @return List of courses in CourseCatalogue
     */
    @Override
    public List<Course> viewCourses() {
        List<Course> courseList = null;
        try {
            courseList = adminDaoInterfaceImpl.viewCourses();
        } catch (Exception e) {
            throw e;
        }
        return courseList;
    }

    /**
     * Method to approve Student
     * @param studentId -> ID of student to be approved
     * @throws StudentNotFoundException
     */
    @Override
    public void approveStudent(String studentId) throws StudentNotFoundException {
        try {
            adminDaoInterfaceImpl.approveStudent(studentId);
        } catch (Exception e) {
            throw e;
        }
    }

    
    /**
     * Method to accept scholarship from students
     */
    public void paymentDoneViaScholarship(String studentID) {
    	NotificationOperation.getInstance().sendNotification(studentID, "payment", "Payment done for student with id " + studentID + " via Scholarship");
    	
    }
    
    /**
     * Method to accept Demand Draft from students
     */
    public void paymentDoneViaDemandDraft(String studentID) {
    	NotificationOperation.getInstance().sendNotification(studentID, "payment", "Payment done for student with id " + studentID + " via Demand Draft");

    }
}
