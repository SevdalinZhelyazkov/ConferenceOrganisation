$(document).ready(function() {
	isAuthUser();

	loadConferenceData();

	var editConferenceButton = $('#edit-conference-button');

	editConferenceButton.on('click', function() {
		editConference();
	});
});

function isAuthUser() {
	$.getJSON('rest/user/current', function(response) {
		if (!response) {
			window.location.replace('index.html');
		}
	});
}

function loadConferenceData() {
	var eventId = sessionStorage.getItem('editConferenceId');

	$.getJSON('rest/events/eventByEventId?eventId=' + eventId, function(response) {
		var eventData = response.event;

		var title = eventData.title,
			description = eventData.description,
			date = eventData.date.split(' ')[0],
			time = eventData.date.split(' ')[1],
			price = eventData.price,
			location = eventData.hall.location,
			hallName = eventData.hall.name,
			city = eventData.hall.city,
			seats = eventData.hall.capacity,
			hallId = eventData.hall.hallId;
		

		$('#eventTopic').val(title);
		$('#eventDescription').val(description);
		$('#eventDate').val(date);
		$('#eventTime').val(time);
		$('#eventPrice').val(price);
		$('#eventAddress').val(location);
		$('#eventHall').val(hallName);
		$('#eventCity').val(city);
		$('#eventSeats').val(seats);
		$('#hallId').val(hallId);

		$.getJSON('rest/lectures/getByEventId?eventId=' + eventId, function(res) {
        	var lecturesData = res.lecture;

        	var len = lecturesData.length;

        	if (len === 0) {
        		$('#edit-lectures-table').append('<div class="alert alert-info">Currently there are no lectures for this event!</div>');
        	}
        	else {
        		for (var i = 0; i < len; i++) {
        			var id = lecturesData[i].lectureId,
        				lectureTitle = lecturesData[i].title,
        				lecturerName = lecturesData[i].lecturerName;

        			var lectureHtml = '<tr id="' + id + '">' +
                    	'<td class="managerTable__title">' +
                        	'<a href="editlecture.html" class="edit-lecture">' + lectureTitle + '</a>' +
                    	'</td>' +
                    	'<td class="managerTable__date">' + lecturerName + '</td>' +
                    	'<td class="managerTable__CTA">' +
                        	'<button class="btn btn-primary btn-xs edit-lecture">Edit</button> ' +
                        	'<button class="btn btn-danger btn-xs delete-lecture">Delete</button> ' +
                    	'</td>' +
                	'</tr>';

                    $('#edit-lectures-table').append(lectureHtml);
        		}
        	}
		}).done(function() {
			$('.edit-lecture').on('click', function(e) {
				var target = e.currentTarget;
				var parent = $(target).parent();
				var grandParent = $(parent).parent();
				var lectureId = $(grandParent).attr('id');
				sessionStorage.setItem('editLectureId', lectureId);
				window.location.href = 'editlecture.html';
			});

			$('.delete-lecture').on('click', function(e) {
				var target = e.currentTarget;
				var parent = $(target).parent();
				var grandParent = $(parent).parent();
				var lectureId = $(grandParent).attr('id');
				deleteLecture(lectureId);
			});
		});
	});
}

function deleteLecture(lectureId) {
	$.ajax({
		type: 'POST',
		url: 'rest/lectures/delete?lectureId=' + lectureId
	}).done(function() {
		toastr.success('Successfully deleted lecture!');
		setTimeout(function() { 
			window.location.reload();
		}, 1000);
	}).fail(function() {
		toastr.error('Failed to delete lecture!');
	});

}

function editConference() {
	var title = $('#eventTopic').val(),
		description = $('#eventDescription').val(),
		date = $('#eventDate').val(),
		time = $('#eventTime').val(),
		price = $('#eventPrice').val(),
		location = $('#eventAddress').val(),
		hallName = $('#eventHall').val(),
		city = $('#eventCity').val(),
		seats = $('#eventSeats').val(),
		hallId = $('#hallId').val();
	
	var eventId = sessionStorage.getItem('editConferenceId');

	var conferenceData = {
		event: {
			eventId: eventId, 
			title: title,
			description: description,
			date: date + ' ' + time,
			price: price,
			hall: {
				hallId: hallId,
				name: hallName,
				location: location,
				city: city,
				capacity: seats
			}
		}
	};

	if (!validateConferenceData(conferenceData)) {
		toastr.error('Invalid data!');
        return;
    }

	$.ajax({
		type: 'POST',
		url: 'rest/events/edit',
		contentType: 'application/json',
		data: JSON.stringify(conferenceData)
	}).done(function() {
		toastr.success('Successfully edited conference!');
		setTimeout(function() { 
			window.location.reload();
		}, 1000);
	}).fail(function() {
		toastr.error('Invalid data! Cannot edit conference!');
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