$(document).ready(function() {
	console.log('here');

	isAuthUser();

	var addLectureButton = $('#add-lecture-button');

	addLectureButton.on('click', function() {
		addNewLecture();
	});
});

function isAuthUser() {
	$.getJSON('rest/user/current', function(response) {
		if (!response) {
			window.location.replace('index.html');
		}
	});
}

function addNewLecture() {
	var eventId = sessionStorage.getItem('conferenceId');

	console.log(eventId);

	if (!eventId) {
		window.location.replace('index.html');
	}

	var lectureTitle = $('#lectureTopic').val(),
		lectureDescription = $('#lectureDescription').val(),
		lecturerName = $('#lecturerName').val(),
		lecturerDescription = $('#lecturerInfo').val(),
		lecturerEmail = $('#lecturerEmail').val();

	var lectureData = {
		lecture: {
			eventId: eventId,
			title: lectureTitle,
			description: lectureDescription,
			lecturerName: lecturerName,
			lecturerDescription: lecturerDescription,
			lecturerEmail: lecturerEmail
		}
	};

	console.log(JSON.stringify(lectureData));

	if (!validateLectureData(lectureData)) {
        alert('Invalid data!');
        return;
    }

	$.ajax({
		type: 'POST',
		url: 'rest/lectures/add',
		contentType: 'application/json',
		data: JSON.stringify(lectureData)
	}).done(function() {
		alert('Successfully added new lecture!')
		window.location.replace('eventmanager.html');
	}).fail(function() {
		alert('Invalid data!');
	});
}

function validateLectureData(lectureData) {
	var lecture = lectureData.lecture;

    function validateLength(str, min, max) {
        return str.length >= min && str.length <= max;
    }

    function validateIfEmpty(str) {
        if (str.trim() === null || str.trim() === '' || str.trim() === ' ') {
            return true;
        }

        return false;
    }

    function validateEmail(email) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    if (validateIfEmpty(lecture.title) || !validateLength(lecture.title, 3, 50) || validateIfEmpty(lecture.description) || !validateLength(lecture.description, 5, 5000) || validateIfEmpty(lecture.lecturerName) || !validateLength(lecture.lecturerName, 2, 35) || validateIfEmpty(lecture.lecturerDescription) || !validateLength(lecture.lecturerDescription, 5, 5000) || !validateEmail(lecture.lecturerEmail)) {
        return false;
    }

    return true;
 }