<?php 

	function send_notification ($tokens, $message)
	{
		$url = 'https://fcm.googleapis.com/fcm/send';
		$fields = array(
			 'registration_ids' => $tokens,
			 'data' => $message
			);

		$headers = array(
			'Authorization:key = AAAAlqsi1WU:APA91bE7fFcSpUT7atawoAOL6OqV2WxG9NS4F9Dfm09iT9yX02Ac-XSpSJ13ZHRLBGt-i0BIaTLJCs7anribX9Zbr4qatzEJooKJQ583hOlBvrS16hMVDS482V5JKfXw4w16z9wx6M94 ',   //add my key!!!!!!!
			'Content-Type: application/json'
			);

	   $ch = curl_init();
       curl_setopt($ch, CURLOPT_URL, $url);
       curl_setopt($ch, CURLOPT_POST, true);
       curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
       curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
       curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);  
       curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
       curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
       $result = curl_exec($ch);  
	   echo "       sending notification      ";             
       if ($result === FALSE) {
		   echo "       fail      ";  
           die('Curl failed: ' . curl_error($ch));
       }
       curl_close($ch);
	   echo "       success      ";  
       return $result;
	}

	function connect2SQL ()
	{
	    // Connect to CloudSQL from App Engine.
	    $dsn = getenv('MYSQL_DSN');
	    $user = getenv('MYSQL_USER');
	    $password = getenv('MYSQL_PASSWORD');
	    if (!isset($dsn, $user) || false === $password) {
	        throw new Exception('Set MYSQL_DSN, MYSQL_USER, and MYSQL_PASSWORD environment variables');
	    }

	    $db = new PDO($dsn, $user, $password);

	    return $db;
	};
	

	if (isset($_POST["ID"]) && isset($_POST["MSG"])) {

		$msg=$_POST["MSG"];
		#$id = $_POST["ID"];
		$id = $_POST["ID"];;    //test************************************************************************
		echo "get id= ".$id.", message= ".$msg;
		$conn = connect2SQL();

		$sql = "SELECT COUNT(*) From users WHERE ID = '$id'";
		
		$result = $conn->query($sql);
		$tokens = array();
		echo "     to find whether Token exist       ";

		if($result -> fetchColumn() >0){
			echo "    find ID's Token    ";
			$q = "SELECT Token From users WHERE ID = '$id'";
			$res = $conn->query($q);
			foreach ($res as $row){
				$tokens[] = $row['Token'];
				echo $row['Token'];
			}
			
			#while ($row = mysqli_fetch_assoc($result)) {
			#	$tokens[] = $row["Token"];
			#}
			$message = array("message" => $msg);
			$message_status = send_notification($tokens, $message);
			#print_r($message);
			#echo "          ";
			#print_r($tokens);
			echo $message_status;
			// foreach ($tokens as $t){
			// 	echo $t;
			// }
			//print_r($tokens);
		}
		else{
			echo "       no token for this ID     ";
		}

		$conn = null;


	}
	#echo "Change the World!"
 ?>
