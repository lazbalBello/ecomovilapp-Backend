meta:
  id: jt808_protocol
  title: Protocolo JT808 (Ecomoviles Edition)
  endian: be
seq:
  - id: start_flag
    contents: [0x7e]
  - id: header
    type: msg_header
  - id: body
    size: header.body_len
    type:
      switch-on: header.msg_id
      cases:
        0x0200: location_report
  - id: check_sum
    type: u1
  - id: end_flag
    contents: [0x7e]

types:
  msg_header:
    seq:
      - id: msg_id
        type: u2
      - id: body_props
        type: u2
      - id: terminal_id
        size: 6
        doc: "Número de teléfono del terminal (BCD). Mapear a vehicleId"
      - id: msg_serial_no
        type: u2
    instances:
      body_len:
        value: body_props & 0x03FF

  location_report:
    seq:
      - id: alarm_flag
        type: u4
      - id: status_flag
        type: u4
      - id: latitude_raw
        type: u4
        doc: "Mapear a latitude: latitude_raw / 1000000.0"
      - id: longitude_raw
        type: u4
        doc: "Mapear a longitude: longitude_raw / 1000000.0"
      - id: altitude
        type: u2
      - id: speed_raw
        type: u2
        doc: "Mapear a speed: speed_raw / 10.0 (km/h)"
      - id: direction
        type: u2
      - id: time_bcd
        size: 6
        doc: "YYMMDDHHMMSS en formato BCD. Mapear a timestamp"
      - id: extra_info
        type: extra_info_item
        repeat: eos
        doc: "Aquí buscaremos el batteryLevel si existe"

  extra_info_item:
    seq:
      - id: id
        type: u1
      - id: len
        type: u1
      - id: value
        size: len