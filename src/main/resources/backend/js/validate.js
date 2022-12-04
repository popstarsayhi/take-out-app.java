
function isValidUsername (str) {
  return ['admin', 'editor'].indexOf(str.trim()) >= 0;
}

function isExternal (path) {
  return /^(https?:|mailto:|tel:)/.test(path);
}

function isCellPhone (val) {
  // (123) 456-7890
  // (123)456-7890
  // 123-456-7890
  // 123.456.7890
  // 1234567890
  // +31636363634
  // 075-63546725

  if (!/^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$/.test(val)) {
    return false
  } else {
    return true
  }
}

//verify account
function checkUserName (rule, value, callback){
  if (value == "") {
    callback(new Error("please enter username"))
  } else if (value.length > 20 || value.length <3) {
    callback(new Error("length of username should be 3-20"))
  } else {
    callback()
  }
}

//verify name
function checkName (rule, value, callback){
  if (value == "") {
    callback(new Error("please enter name"))
  } else if (value.length > 12) {
    callback(new Error("length of name should be 1-12"))
  } else {
    callback()
  }
}

function checkPhone (rule, value, callback){
  // let phoneReg = /(^1[3|4|5|6|7|8|9]\d{9}$)|(^09\d{8}$)/;
  if (value == "") {
    callback(new Error("please enter phone number"))
  } else if (!isCellPhone(value)) {//import methods of checking phone number format
    callback(new Error("please enter the correct phone number"))
  } else {
    callback()
  }
}


function validID (rule,value,callback) {
  //either 15 or 18 works，15: all numbers，18: 17 numbers + x or number
  let reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if(value == '') {
    callback(new Error('please enter personal ID'))
  } else if (reg.test(value)) {
    callback()
  } else {
    callback(new Error('personal ID is not corrected'))
  }
}