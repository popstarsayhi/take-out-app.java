function getMemberList (params) {
  return $axios({
    url: '/employee/page',
    method: 'get',
    params
  })
}

// update---enable disable interface
function enableOrDisableEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

// add --- add employee
function addEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'post',
    data: { ...params }
  })
}

// update--update employee info
function editEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

// Modify the page to check the details interface
function queryEmployeeById (id) {
  return $axios({
    url: `/employee/${id}`,
    method: 'get'
  })
}