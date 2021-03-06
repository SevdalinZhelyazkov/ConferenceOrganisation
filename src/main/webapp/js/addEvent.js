$(document).ready(function() {
	isAuthUser();

	var addConferenceButton = $('#add-conference-button');

	addConferenceButton.on('click', function() {
		addNewConference();
	});
});

function isAuthUser() {
	$.getJSON('rest/user/current', function(response) {
		if (!response) {
			window.location.replace('index.html');
		}
	});
}

function addNewConference() {
	var title = $('#eventTopic').val(),
		description = $('#eventDescription').val(),
		date = $('#eventDate').val(),
		time = $('#eventTime').val(),
		price = $('#eventPrice').val(),
		location = $('#eventAddress').val(),
		hallName = $('#eventHall').val(),
		city = $('#eventCity').val(),
		seats = $('#eventSeats').val();

	var conferenceData = {
		event: {
			title: title,
			description: description,
			date: date + ' ' + time,
			price: price,
			hall: {
				name: hallName,
				location: location,
				city: city,
				capacity: seats
			},
			status: 'NEW'
		}
	};

	if (!validateConferenceData(conferenceData)) {
		toastr.error('Invalid data!');
        return;
    }

	$.ajax({
		type: 'POST',
		url: 'rest/user/createEvent',
		contentType: 'application/json',
		data: JSON.stringify(conferenceData)
	}).done(function(res) {
		var conferenceId = res;
		sessionStorage.setItem("conferenceId", conferenceId);
		toastr.success('Successfully added new conference!');
		setTimeout(function() { 
			window.location.replace('addlecture.html');
		}, 1000);
		
	}).fail(function() {
		toastr.error('Invalid data!');
	});
}

function validateConferenceData(conferenceData) {
	var event = conferenceData.event;

    function validateLength(str, min, max) {
        return str.length >= min && str.length <= max;
    }

    function validateIfEmpty(str) {
        if (str.trim() === null || str.trim() === '' || str.trim() === ' ') {
            return true;
        }

        return false;
    }

    if (validateIfEmpty(event.title) || !validateLength(event.title, 4, 40) || event.price < 0 || event.hall.capacity <= 0) {
        return false;
    }

    return true;
 }