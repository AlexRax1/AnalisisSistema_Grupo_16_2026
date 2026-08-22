import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrearQueja } from './crear-queja';

describe('CrearQueja', () => {
  let component: CrearQueja;
  let fixture: ComponentFixture<CrearQueja>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearQueja],
    }).compileComponents();

    fixture = TestBed.createComponent(CrearQueja);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
