<?php
	include('connect.php');

	$empID = $_POST['empID'];

	$query = mysqli_query($con, "SELECT empID FROM emp_parking where empID = '" . $empID . "';");
	
	if (mysqli_num_rows($query) > 0) {
		$loginTime = $_POST['loginTime'];
		$logoutTime = $_POST['logoutTime'];
		
		if (mysqli_query($con, "UPDATE emp_parking SET loginTime = '" . $loginTime . "', logoutTime = '" . $logoutTime . "' WHERE empID = '" . $empID . "';")) {
			echo 'Success';
		} else {
			echo 'Fail';
		}
	} else {
		echo 'Invalid';
	}
?>