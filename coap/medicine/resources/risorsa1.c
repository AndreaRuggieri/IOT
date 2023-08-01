#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "res_1"
#define LOG_LEVEL LOG_LEVEL_APP

extern uint8_t led;

static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_number_1,
         "title=\"Light Switch\";rt=\"Control\"",
         NULL,
         NULL,
         light_put_handler,
         NULL);

bool light_on = false;

static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
		
	len = coap_get_post_variable(request, "risorsa1", &text);
	if(len <= 0 || len >= 4)
		goto error;
	
	if(strncmp(text, "ON", len) == 0) {
		light_on = true;
		leds_set(led);
		LOG_INFO("Light ON\n");
	} else if(strncmp(text, "OFF", len) == 0) {
		light_on = false;
		leds_off(LEDS_ALL);
		LOG_INFO("Light OFF\n");
	}
	else
		goto error;

	return;
error:
	coap_set_status_code(response, BAD_REQUEST_4_00);
}