<?php
/*
* Author: Rohit Kumar
* Website: iamrohit.in
* Version: 0.0.1
* Date: 14-04-2015
* App Name: Email Validator
* Description: Simple email id validator to check email id is valid of fake.
*/
//error_reporting(0);
ob_start();
header('Content-Type: application/json');
include_once("classes/location.php");

$loc = new location();			

try {
  if(!isset($_GET['type']) || empty($_GET['type'])) {
  	throw new exception("Type is not set.");
  }
  $type = $_GET['type'];
  if($type=='getCountries') {
  	$data = $loc->getCountries();
  } 

  if($type=='getStates') {
  	 if(!isset($_GET['countryId']) || empty($_GET['countryId'])) {
  	 	throw new exception("Country Id is not set.");
  	 }
  	 $countryId = $_GET['countryId'];
  	 $data = $loc->getStates($countryId);
  }

   if($type=='getCities') {
  	 if(!isset($_GET['stateId']) || empty($_GET['stateId'])) {
  	 	throw new exception("State Id is not set.");
  	 }
  	 $data = $loc->getCities($stateId);
  }

} catch (Exception $e) {
   $data = array('status'=>'error', 'tp'=>0, 'msg'=>$e->getMessage);
} finally {
  echo json_encode($data);
}

ob_flush();






