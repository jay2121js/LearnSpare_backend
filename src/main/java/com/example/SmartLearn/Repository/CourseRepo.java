package com.example.SmartLearn.Repository;

import com.example.SmartLearn.DTO.CourseDTO;
import com.example.SmartLearn.Entity.Course;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {

    /**
     * Find all courses whose name contains the given substring.
     *
     * @param courseName substring to search for
     * @return list of matching Course entities
     */
    List<Course> findByCourseNameContaining(String courseName);

    /**
     * Fetch a summary DTO for every course.
     * Uses a constructor expression to map to CourseDTO.
     *
     * @return list of CourseDTOs with id, title, thumbnailUrl, difficultyLevel, and price
     */
    @Query("SELECT new com.example.SmartLearn.DTO.CourseDTO(" +
            "c.courseId, c.title, c.thumbnailUrl, c.difficultyLevel, c.price, " +
            "c.lessonCount, c.duration) " +
            "FROM Course c")
    List<CourseDTO> fetchCourseSummaries();

    @Query("SELECT new com.example.SmartLearn.DTO.CourseDTO(" +
            "c.courseId, c.title, c.thumbnailUrl, c.difficultyLevel, c.price, " +
            "c.lessonCount, c.duration) " +
            "FROM Student s JOIN s.courses c " +
            "WHERE s.user.id = :userId")
    List<CourseDTO> findEnrolledCoursesByUserId(@Param("userId") Long userId);


    @Query("SELECT new com.example.SmartLearn.DTO.CourseDTO(" +
            "c.courseId, c.title, c.thumbnailUrl, c.difficultyLevel, c.price, " +
            "c.lessonCount, c.duration) " +
            "FROM Teacher s JOIN s.courses c " +
            "WHERE s.user.id = :userId")
    List<CourseDTO> findOwnedCoursesByUserIdLite(@Param("userId") Long userId);

    /**
     * Check if a given user (by userId) is enrolled in a given course.
     * Joins the Student ↔ Course relationship.
     *
     * @param userId   the User's id
     * @param courseId the Course's id
     * @return true if the user is enrolled, false otherwise
     */
    @Query("""
      SELECT COUNT(c) > 0
        FROM Student s
        JOIN s.courses c
       WHERE s.user.id    = :userId
         AND c.courseId   = :courseId
    """)
    boolean isCourseEnrolledByUser(
            @Param("userId")   Long userId,
            @Param("courseId") Long courseId
    );
    @Query("""
  SELECT c.courseId
    FROM Student s
    JOIN s.courses c
   WHERE s.user.id = :userId
""")
    List<Long> findEnrolledCourseIdsByUser(
            @Param("userId") Long userId
    );

    @Query("""
    SELECT c.courseId
    FROM Teacher t
    JOIN t.courses c
    WHERE t.user.id = :userId
""")
    List<Long> findOwnedCourseIdsByUser(@Param("userId") Long userId);

    /**
     * Check if a given user (by userId) owns a given course as its instructor.
     *
     * @param userId   the User's id
     * @param courseId the Course's id
     * @return true if the user is the instructor for that course
     */
    @Query("""
      SELECT COUNT(c) > 0
        FROM Course c
       WHERE c.courseId = :courseId
         AND c.instructor.user.id = :userId
    """)
    boolean isCourseOwnedByUser(
            @Param("userId")   Long userId,
            @Param("courseId") Long courseId
    );

    /**
     * Remove all student–course association entries for a given course.
     * Useful when hard-deleting a course to avoid FK constraint errors.
     *
     * @param courseId the Course's id
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM student_courses WHERE course_id = :courseId", nativeQuery = true)
    void deleteStudentCourseAssociations(@Param("courseId") Long courseId);

    /**
     * Hard-delete a course row by its id.
     * Use with caution: bypasses JPA’s normal delete cascade.
     *
     * @param courseId the Course's id
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM courses WHERE courseId = :courseId", nativeQuery = true)
    void hardDeleteById(@Param("courseId") Long courseId);

    /**
     * Find a Course that contains a video with the given id.
     * Allows you to look up which course a particular video belongs to.
     *
     * @param videosId the Video’s id
     * @return the matching Course entity (or null if none)
     */
    Course getCourseByVideosId(Long videosId);

    @Query("""
    SELECT new com.example.SmartLearn.DTO.CourseDTO(
        c.courseId, c.title, c.thumbnailUrl, c.difficultyLevel, c.price, c.lessonCount, c.duration
    )
    FROM Course c
    WHERE
        (:category IS NULL OR c.category = :category)
        AND (:difficulty IS NULL OR c.difficultyLevel = :difficulty)
        AND (
            :search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :search, '%'))
        )
""")
    Page<CourseDTO> findBestCoursesByFiltersDTO(
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"videos"})
    @Query("SELECT c FROM Course c WHERE c.courseId = :courseId")
    Course findCourseWithVideosById(@Param("courseId") Long courseId);



}
