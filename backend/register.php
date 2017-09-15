<?php 

	function connect2SQL ()
	{
	    // Connect to CloudSQL from App Engine.
	    $dsn = getenv('MYSQL_DSN');
	    $user = getenv('MYSQL_USER');
	    $password = getenv('MYSQL_PASSWORD');
		$host = getenv('DEVELOPMENT_DB_HOST');
		$dbName = getenv('PRODUCTION_DB_NAME');
	    if (!isset($dsn, $user, $dbName, $host) || false === $password) {
	        throw new Exception('Set MYSQL_DSN, MYSQL_USER, and MYSQL_PASSWORD environment variables');
	    }

	    $db = new PDO($dsn, $user, $password);
		

	    return $db;
	};
	
	if (isset($_POST["Token"]) && isset($_POST["ID"])) {
		
		$_uv_Token=$_POST["Token"];
		$id = $_POST["ID"];
		echo "get id= ".$id.", token= ".$_uv_Token;
	   	$conn = connect2SQL();
	   	#$stmt = $conn->prepare('INSERT INTO entries (token, ID) VALUES (:Token, :id) ON DUPLICATE KEY UPDATE ');
    	#$stmt->execute([
        #	':Token' => $_uv_Token,
        #	':id' => $id,
    	#]);
    	$sql = "SELECT COUNT(*) From users WHERE ID = '$id'";
    	#$result = mysqli_query($conn,$sql);
		$result = $conn->query($sql);
	
		if($result -> fetchColumn() >0){
			//there is same ID
			echo "    same ID    ";
			$q = "UPDATE users SET Token = '$_uv_Token' WHERE ID = '$id'";
			$conn -> query($q);
		}
		else{
			echo "   no same ID    ";
			$q = "INSERT INTO users (Token, ID) VALUES ( '$_uv_Token', '$id') ";
			$conn -> query($q);
		}
          
     	#mysqli_query($conn,$q) or die(mysqli_error($conn));

      	$conn = null;

	}
	#echo "fuckgoogle!"

 ?>
