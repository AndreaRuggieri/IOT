#include "contiki.h"
#include "coap-engine.h"
#include "res-heartbeat.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  const char *len = NULL;
  char const *const message = "RISORSA BATTITO ATTIVATA";
  int length = 25;
  
  if(coap_get_query_variable(request, "len", &len)) 
  {
    length = atoi(len);
    if(length<0) 
    {
        length = 0;
    } 
    if(length>64)
    {
        length = 64;
    }
    memcpy(buffer, message, length);
  }
  else
  {
    memcpy(buffer, message, length);
  }
  
  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_header_etag(response, (uint8_t *)&length, 1);
  coap_set_payload(response, buffer, length);
}

RESOURCE(res_heartbeat,
         "title=\"hb: ?len=0..\";rt=\"Text\"",
		 res_get_handler,
         NULL,
         NULL,
         NULL);
