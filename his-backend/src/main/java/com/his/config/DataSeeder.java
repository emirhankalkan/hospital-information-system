package com.his.config;

import com.his.entity.Appointment;
import com.his.entity.Department;
import com.his.entity.Doctor;
import com.his.entity.Patient;
import com.his.entity.Role;
import com.his.entity.User;
import com.his.enums.AppointmentStatus;
import com.his.enums.Gender;
import com.his.enums.RoleName;
import com.his.repository.AppointmentRepository;
import com.his.repository.DepartmentRepository;
import com.his.repository.DoctorRepository;
import com.his.repository.PatientRepository;
import com.his.repository.RoleRepository;
import com.his.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String SAMPLE_PASSWORD = "Password123!";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();

        if (userRepository.count() > 0) {
            log.info("Örnek veri zaten mevcut. Seeder atlandı.");
            return;
        }

        seedSampleData();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            log.info("Rol bulunamadı. Temel roller ekleniyor...");
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            });
            log.info("Temel roller eklendi.");
        }
    }

    private void seedSampleData() {
        log.info("Örnek HIS verisi ekleniyor...");

        Role adminRole = findRole(RoleName.ADMIN);
        Role receptionistRole = findRole(RoleName.RECEPTIONIST);
        Role doctorRole = findRole(RoleName.DOCTOR);
        Role patientRole = findRole(RoleName.PATIENT);

        User admin = createUser("admin", "admin@his.local", adminRole);
        User receptionist = createUser("receptionist", "receptionist@his.local", receptionistRole);
        User doctorUserOne = createUser("dr.aylin", "aylin.kaya@his.local", doctorRole);
        User doctorUserTwo = createUser("dr.murat", "murat.demir@his.local", doctorRole);
        User patientUserOne = createUser("patient.elif", "elif.yilmaz@example.com", patientRole);
        User patientUserTwo = createUser("patient.can", "can.arslan@example.com", patientRole);
        User patientUserThree = createUser("patient.zeynep", "zeynep.sahin@example.com", patientRole);

        Department cardiology = createDepartment("Kardiyoloji", "Kalp ve damar hastalıkları");
        Department neurology = createDepartment("Nöroloji", "Beyin ve sinir sistemi hastalıkları");
        Department pediatrics = createDepartment("Çocuk Sağlığı", "Çocuk sağlığı ve hastalıkları");

        Doctor doctorOne = createDoctor(doctorUserOne, cardiology, "Aylin", "Kaya", "Kardiyoloji", "5551001001");
        Doctor doctorTwo = createDoctor(doctorUserTwo, neurology, "Murat", "Demir", "Nöroloji", "5551001002");

        Patient patientOne = createPatient(patientUserOne, "Elif", "Yılmaz", "10000000001",
                LocalDate.of(1992, 4, 12), Gender.FEMALE, "5552001001", "A+");
        Patient patientTwo = createPatient(patientUserTwo, "Can", "Arslan", "10000000002",
                LocalDate.of(1988, 9, 3), Gender.MALE, "5552001002", "0+");
        Patient patientThree = createPatient(patientUserThree, "Zeynep", "Şahin", "10000000003",
                LocalDate.of(2016, 1, 21), Gender.FEMALE, "5552001003", "B+");

        createAppointment(patientOne, doctorOne, receptionist, LocalDate.now().plusDays(1), LocalTime.of(9, 0),
                "İlk kardiyoloji muayenesi");
        createAppointment(patientTwo, doctorOne, receptionist, LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                "Tansiyon kontrol randevusu");
        createAppointment(patientThree, doctorTwo, receptionist, LocalDate.now().plusDays(2), LocalTime.of(11, 30),
                "Baş ağrısı değerlendirmesi");

        log.info("Örnek veri eklendi. Tüm örnek kullanıcıların şifresi: {}", SAMPLE_PASSWORD);
        log.info("Örnek kullanıcılar: {}, {}, {}, {}, {}, {}, {}",
                admin.getUsername(),
                receptionist.getUsername(),
                doctorUserOne.getUsername(),
                doctorUserTwo.getUsername(),
                patientUserOne.getUsername(),
                patientUserTwo.getUsername(),
                patientUserThree.getUsername());
        log.info("Örnek departmanlar: {}, {}, {}", cardiology.getName(), neurology.getName(), pediatrics.getName());
    }

    private Role findRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol bulunamadı: " + roleName));
    }

    private User createUser(String username, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(SAMPLE_PASSWORD));
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private Department createDepartment(String name, String description) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    private Doctor createDoctor(User user, Department department, String firstName, String lastName,
                                String specialization, String phone) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        return doctorRepository.save(doctor);
    }

    private Patient createPatient(User user, String firstName, String lastName, String tcNo,
                                  LocalDate birthDate, Gender gender, String phone, String bloodType) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setTcNo(tcNo);
        patient.setBirthDate(birthDate);
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setEmail(user.getEmail());
        patient.setAddress("Örnek adres");
        patient.setEmergencyContact("5559990000");
        patient.setBloodType(bloodType);
        patient.setIsDeleted(false);
        return patientRepository.save(patient);
    }

    private Appointment createAppointment(Patient patient, Doctor doctor, User createdByUser,
                                          LocalDate date, LocalTime time, String notes) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setCreatedByUser(createdByUser);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(notes);
        return appointmentRepository.save(appointment);
    }
}
